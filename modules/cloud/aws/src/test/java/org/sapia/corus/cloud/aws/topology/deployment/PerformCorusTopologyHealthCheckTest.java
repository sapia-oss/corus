package org.sapia.corus.cloud.aws.topology.deployment;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.sapia.corus.cloud.aws.client.InstanceStatusCode;
import org.sapia.corus.cloud.platform.domain.CorusAddress;
import org.sapia.corus.cloud.platform.http.HttpClientFacade;
import org.sapia.corus.cloud.platform.http.HttpClientFactory;
import org.sapia.corus.cloud.platform.http.HttpResponse;
import org.sapia.corus.cloud.platform.rest.CorusCredentials;
import org.sapia.corus.cloud.platform.util.TimeSupplier;
import org.sapia.corus.cloud.platform.util.TimeSupplier.MutableTime;
import org.sapia.corus.cloud.platform.workflow.exceptions.AbortedDeploymentException;
import org.sapia.corus.cloud.topology.Cluster;
import org.sapia.corus.cloud.topology.Env;
import org.sapia.corus.cloud.topology.Region;
import org.sapia.corus.cloud.topology.Topology;
import org.sapia.corus.cloud.topology.Zone;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;

@RunWith(MockitoJUnitRunner.class)
public class PerformCorusTopologyHealthCheckTest {
  
  @Mock
  private AmazonEC2 ec2Client;
  
  @Mock
  private AmazonCloudFormation cloudFormationClient;
  
  @Mock
  private HttpClientFactory clientFactory;
  
  @Mock
  private HttpClientFacade client;
  
  @Mock
  private HttpResponse pendingResponse;
  
  private MutableTime clock = MutableTime.getInstance();
  
  private List<Reservation> reservations;
  
  private PerformCorusTopologyHealthCheck step;
  
  private AwsTopologyDeploymentConf conf;
  
  private AwsTopologyDeploymentContext context;
  
 
  @Before
  public void setUp() throws Exception {
    reservations = new ArrayList<>();
    
    Topology topo = new Topology();
    topo.setOrg("test-org");
    topo.setApplication("test-app");
    topo.setVersion("test-version");
   
    Env env = new Env();
    env.setName("test-env");
    Cluster cluster = new Cluster();
    cluster.setName("test-cluster");
    
    Region region = new Region();
    region.setName("test-region");
    topo.addRegionTemplate(region);
    
    Zone zone = new Zone();
    zone.setName("test-zone");
    
    region.addZone(zone);
    env.addCluster(cluster);
    env.addRegion(region);
    topo.addEnv(env);
    
    conf = new AwsTopologyDeploymentConf()
        .withTopology(topo)
        .withEnvironment("test-env")
        .withCorusCredentials(new CorusCredentials("test-app-id", "test-app-key"));
    
    context = new AwsTopologyDeploymentContext(conf, cloudFormationClient, ec2Client)
        .withHttpClientFactory(clientFactory);
 
    context.withTimeSupplier(clock);
    step = new PerformCorusTopologyHealthCheck();
    
    when(clientFactory.getClient()).thenReturn(client);
    
    when(pendingResponse.getStatusCode()).thenReturn(HttpResponse.STATUS_DIAGNOSTIC_PENDING);
    when(pendingResponse.getStatusMessage()).thenReturn("Diagnostic pending");
  }

  @Test(expected = IllegalStateException.class)
  public void testExecute_no_instance_found() throws Exception {
    when(ec2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(
        new DescribeInstancesResult().withReservations(new ArrayList<Reservation>())
    );
    step.execute(context);
  }
  
  @Test
  public void testExecute_with_repo_public_ip() throws Exception {
    setUpPublicRepoInstance();
    setUpClient(2, HttpResponse.STATUS_OK);
    
    step.execute(context);
    
    String expectedUrl = new CorusAddress("test-public-ip", 33000).asHttpsUrl() + "/rest/clusters/test-cluster/diagnostic";
    verify(client, times(3)).get(eq(expectedUrl), anyLong(), anyMapOf(String.class, String.class));
  }
  
  @Test
  public void testExecute_with_non_repo_public_ip() throws Exception {
    setUpPublicInstance();
    setUpClient(2, HttpResponse.STATUS_OK);
    
    step.execute(context);
    
    String expectedUrl = new CorusAddress("test-public-ip", 33000).asHttpsUrl() + "/rest/clusters/test-cluster/diagnostic";
    verify(client, times(3)).get(eq(expectedUrl), anyLong(), anyMapOf(String.class, String.class));
  }
  
  @Test
  public void testExecute_with_repo_private_ip() throws Exception {
    setUpPrivateRepoInstance();
    setUpClient(2, HttpResponse.STATUS_OK);
    
    step.execute(context);
    
    String expectedUrl = new CorusAddress("test-private-repo-ip", 33000).asHttpsUrl() + "/rest/clusters/test-cluster/diagnostic";
    verify(client, times(3)).get(eq(expectedUrl), anyLong(), anyMapOf(String.class, String.class));
  }
  
  @Test
  public void testExecute_with_non_repo_private_ip() throws Exception {
    setUpPrivateInstance();
    setUpClient(2, HttpResponse.STATUS_OK);
    
    step.execute(context);
    
    String expectedUrl = new CorusAddress("test-private-ip", 33000).asHttpsUrl() + "/rest/clusters/test-cluster/diagnostic";
    verify(client, times(3)).get(eq(expectedUrl), anyLong(), anyMapOf(String.class, String.class));
  }
  
  @Test(expected = AbortedDeploymentException.class)
  public void testExecute_with_server_error() throws Exception {
    setUpPublicRepoInstance();
    setUpClient(2, HttpResponse.STATUS_SERVER_ERROR);
    
    step.execute(context);
  }
  
  @Test(expected = AbortedDeploymentException.class)
  public void testExecute_with_connection_error() throws Exception {
    setUpPublicRepoInstance();
    setUpIoErrorClient();
    
    step.execute(context);
  }
  
  private void setUpPublicRepoInstance() {
    reservations.add(new Reservation().withInstances(
        new Instance().withState(new InstanceState()
            .withName(InstanceStateName.ShuttingDown)
            .withCode(InstanceStatusCode.STOPPED.value())),
        new Instance().withState(new InstanceState()
            .withName(InstanceStateName.Pending)
            .withCode(InstanceStatusCode.PENDING.value())),
        new Instance()
          .withState(new InstanceState()
              .withName(InstanceStateName.Running)
              .withCode(InstanceStatusCode.RUNNING.value()))
          .withPrivateIpAddress("test-private-ip")
          .withTags(new Tag().withKey("corus.repo.role").withValue("server")),
        new Instance()
          .withState(new InstanceState()
              .withName(InstanceStateName.Running)
              .withCode(InstanceStatusCode.RUNNING.value()))
          .withPublicIpAddress("test-public-ip")
          .withTags(new Tag().withKey("corus.repo.role").withValue("server"))
    ));
    when(ec2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(
        new DescribeInstancesResult().withReservations(reservations)
    );
  }
  
  private void setUpPublicInstance() {
    reservations.add(new Reservation().withInstances(
        new Instance().withState(new InstanceState()
            .withName(InstanceStateName.ShuttingDown)
            .withCode(InstanceStatusCode.STOPPED.value())),
        new Instance().withState(new InstanceState()
            .withName(InstanceStateName.Pending)
            .withCode(InstanceStatusCode.PENDING.value())),
        new Instance()
          .withState(new InstanceState()
              .withName(InstanceStateName.Running)
              .withCode(InstanceStatusCode.RUNNING.value()))
          .withPrivateIpAddress("test-private-ip"),
        new Instance()
          .withState(new InstanceState()
              .withName(InstanceStateName.Running)
              .withCode(InstanceStatusCode.RUNNING.value()))
          .withPublicIpAddress("test-public-ip")
    ));
    when(ec2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(
        new DescribeInstancesResult().withReservations(reservations)
    );
  }
  
  private void setUpPrivateRepoInstance() {
    reservations.add(new Reservation().withInstances(
        new Instance().withState(new InstanceState()
            .withName(InstanceStateName.ShuttingDown)
            .withCode(InstanceStatusCode.STOPPED.value())),
        new Instance().withState(new InstanceState()
            .withName(InstanceStateName.Pending)
            .withCode(InstanceStatusCode.PENDING.value())),
        new Instance()
          .withState(new InstanceState()
              .withName(InstanceStateName.Running)
              .withCode(InstanceStatusCode.RUNNING.value()))
          .withPrivateIpAddress("test-private-ip"),
        new Instance()
          .withState(new InstanceState()
              .withName(InstanceStateName.Running)
              .withCode(InstanceStatusCode.RUNNING.value()))
          .withPrivateIpAddress("test-private-repo-ip")
          .withTags(new Tag().withKey("corus.repo.role").withValue("server"))
    ));
    when(ec2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(
        new DescribeInstancesResult().withReservations(reservations)
    );
  }

  private void setUpPrivateInstance() {
    reservations.add(new Reservation().withInstances(
        new Instance().withState(new InstanceState()
            .withName(InstanceStateName.ShuttingDown)
            .withCode(InstanceStatusCode.STOPPED.value())),
        new Instance().withState(new InstanceState()
            .withName(InstanceStateName.Pending)
            .withCode(InstanceStatusCode.PENDING.value())),
        new Instance()
          .withPrivateIpAddress("test-private-ip")
          .withState(new InstanceState()
              .withName(InstanceStateName.Running)
              .withCode(InstanceStatusCode.RUNNING.value()))
    ));
    when(ec2Client.describeInstances(any(DescribeInstancesRequest.class))).thenReturn(
        new DescribeInstancesResult().withReservations(reservations)
    );
  }
  
  private void setUpClient(final int numInProgress, final int statusCode) throws Exception { 
   
    doAnswer(new Answer<HttpResponse>() {
      int count = 0;
      @Override
      public HttpResponse answer(InvocationOnMock invocation) throws Throwable {
        if (count < numInProgress) {
          count++;
          return pendingResponse;
        } else {
          count++;
          HttpResponse finalResponse = mock(HttpResponse.class);
          when(finalResponse.getStatusCode()).thenReturn(statusCode);
          when(finalResponse.getStatusMessage()).thenReturn("status-message-" + statusCode);
          return finalResponse;
        }
      }
    }).when(client).get(anyString(), anyLong(), anyMapOf(String.class, String.class));
    
  }

  private void setUpIoErrorClient() throws Exception { 
    
   when(
       client.get(anyString(), anyLong(), anyMapOf(String.class, String.class))
   ).thenThrow(new IOException("I/O error"));
    
  }
}
