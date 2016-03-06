package org.sapia.corus.cloud.aws.topology.deployment.helper;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.cloud.aws.topology.deployment.AwsTopologyDeploymentConf;
import org.sapia.corus.cloud.aws.topology.deployment.AwsTopologyDeploymentContext;
import org.sapia.corus.cloud.platform.domain.CorusAddress;
import org.sapia.corus.cloud.platform.domain.CorusInstance;
import org.sapia.corus.cloud.platform.rest.CorusCredentials;
import org.sapia.corus.cloud.platform.rest.CorusRestClient;
import org.sapia.corus.cloud.platform.rest.CorusRestClientFactory;
import org.sapia.corus.cloud.platform.rest.JSONValue;
import org.sapia.corus.cloud.platform.util.Input;
import org.sapia.corus.cloud.platform.workflow.exceptions.AbortedDeploymentException;
import org.sapia.corus.cloud.topology.Cluster;
import org.sapia.corus.cloud.topology.Env;
import org.sapia.corus.cloud.topology.Region;
import org.sapia.corus.cloud.topology.Topology;
import org.sapia.corus.cloud.topology.Zone;

import com.amazonaws.services.cloudformation.AmazonCloudFormationClient;
import com.amazonaws.services.ec2.AmazonEC2;
import com.google.common.base.Function;

import net.sf.json.JSONObject;

@RunWith(MockitoJUnitRunner.class)
public class DeploymentStepSupportTest {
  
  @Mock
  private CorusRestClientFactory clientFactory;

  @Mock
  private CorusRestClient client;
  
  @Mock
  private AmazonCloudFormationClient cloudFormationClient;
  
  @Mock
  private AmazonEC2 ec2Client;

  @Mock
  private Input input1, input2;
  
  @Mock
  private WebTarget deployResource, progressResource;
  
  @Mock
  private Invocation.Builder deployRequest, progressRequest;
  
  @Mock
  private Response deployResponse;
  
  @Mock
  private List<Input> inputList;
  
  private AwsTopologyDeploymentContext context;
  
  private AwsTopologyDeploymentConf conf;

  private DeploymentStepSupport step;
  
  @Before
  public void setUp() throws Exception {
    
    inputList = new ArrayList<>();
    inputList.add(input1);
    inputList.add(input2);

    Topology t = new Topology();
    Env env = new Env();
    env.setName("test-env");
    
    conf    = new AwsTopologyDeploymentConf()
        .withCorusCredentials(new CorusCredentials("test-app-id", "test-app-key"))
        .withDistributions(inputList)
        .withTopology(t).withEnvironment("test-env");
   
    context = new AwsTopologyDeploymentContext(conf, cloudFormationClient, ec2Client).withRestClientFactory(clientFactory);
    
    step = new DeploymentStepSupport(
        "TEST", 
        new Function<AwsTopologyDeploymentContext, List<Input>>() {
      
          @Override
          public List<Input> apply(AwsTopologyDeploymentContext context) {
            return inputList;
          }
        },
        new Function<CorusInstance, String>() {
          public String apply(CorusInstance instance) {
            return "deploy-resource";
          }
        }
    ) {
      // noop
    };

    Region r = new Region();
    r.setName("test-region");
    Zone z = new Zone();
    z.setName("test-zone");
    Cluster c = new Cluster();
    c.setName("test-cluster");

    CorusInstance instance = new CorusInstance(r, z, env, c, new CorusAddress("test-corus", 33000));
    context.addCorusInstance(instance);
    
    when(input1.getInfo()).thenReturn("input1");
    when(input1.getInputStream()).thenReturn(new ByteArrayInputStream("input1-stream".getBytes()));
    when(input2.getInfo()).thenReturn("input2");
    when(input2.getInputStream()).thenReturn(new ByteArrayInputStream("input2-stream".getBytes()));
    
    when(client.resource(eq("deploy-resource"))).thenReturn(deployResource);
    when(deployResource.request()).thenReturn(deployRequest);
    when(deployResource.queryParam(anyString(), isA(Object.class))).thenReturn(deployResource);
    when(deployRequest.accept(any(MediaType.class))).thenReturn(deployRequest);
    when(deployRequest.put(isA(Entity.class))).thenReturn(deployResponse);
    
    when(client.resource(eq("/progress/test-completion-token"))).thenReturn(progressResource);
    when(progressResource.request()).thenReturn(progressRequest);
    when(progressResource.queryParam(anyString(), isA(Object.class))).thenReturn(progressResource);
    when(progressRequest.accept(any(MediaType.class))).thenReturn(progressRequest);
    
    when(clientFactory.getClient(any(CorusAddress.class), any(CorusCredentials.class))).thenReturn(client);
  }

  @Test
  public void testGetDescription() {
    assertEquals("TEST", step.getDescription());
  }
  
  @Test(expected = AbortedDeploymentException.class)
  public void testExecute_status_error() throws Exception {
    setUpDeployErrorResponse();
    step.execute(context);
  }
  
  @Test
  public void testExecute_progress_success() throws Exception {
    setUpDeploySuccessResponse();
    setUpProgressSuccessResponse();
    step.execute(context);
  }
  
  @Test(expected = AbortedDeploymentException.class)
  public void testExecute_progress_error() throws Exception {
    setUpDeploySuccessResponse();
    setUpProgressErrorResponse();
    step.execute(context);
  }
  
  private void setUpDeploySuccessResponse() {
    JSONObject json = new JSONObject();
    json.element("status", CorusRestClient.STATUS_OK);
    json.element("completionToken", "test-completion-token");
    
    when(deployResponse.getStatus()).thenReturn(CorusRestClient.STATUS_OK);
    when(deployResponse.readEntity(JSONValue.class)).thenReturn(new JSONValue(json));
  }

  private void setUpDeployErrorResponse() {
    JSONObject json = new JSONObject();
    json.element("status", CorusRestClient.STATUS_SERVER_ERROR);
    
    when(deployResponse.getStatus()).thenReturn(CorusRestClient.STATUS_SERVER_ERROR);
    when(deployResponse.readEntity(JSONValue.class)).thenReturn(new JSONValue(json));
  }
  
  private void setUpProgressSuccessResponse() {
    JSONObject json = new JSONObject();
    json.element("status", CorusRestClient.STATUS_OK);
    
    when(progressRequest.get(JSONValue.class)).thenReturn(new JSONValue(json));
  }
  
  private void setUpProgressErrorResponse() {
    JSONObject json = new JSONObject();
    json.element("status", CorusRestClient.STATUS_SERVER_ERROR);
    
    when(progressRequest.get(JSONValue.class)).thenReturn(new JSONValue(json));
  }
}
