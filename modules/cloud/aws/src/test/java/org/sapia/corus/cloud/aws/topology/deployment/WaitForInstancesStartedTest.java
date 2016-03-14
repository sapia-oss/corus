package org.sapia.corus.cloud.aws.topology.deployment;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;

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
import org.sapia.corus.cloud.platform.rest.CorusCredentials;
import org.sapia.corus.cloud.platform.util.RetryCriteria;
import org.sapia.corus.cloud.platform.util.Sleeper;
import org.sapia.corus.cloud.platform.util.TimeMeasure;
import org.sapia.corus.cloud.platform.util.TimeSupplier.MutableTime;
import org.sapia.corus.cloud.platform.workflow.exceptions.AbortedDeploymentException;
import org.sapia.corus.cloud.topology.Cluster;
import org.sapia.corus.cloud.topology.Env;
import org.sapia.corus.cloud.topology.Machine;
import org.sapia.corus.cloud.topology.Topology;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.ListStackResourcesRequest;
import com.amazonaws.services.cloudformation.model.ListStackResourcesResult;
import com.amazonaws.services.cloudformation.model.StackResourceSummary;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.InstanceState;
import com.amazonaws.services.ec2.model.InstanceStatus;

@RunWith(MockitoJUnitRunner.class)
public class WaitForInstancesStartedTest {
  
  private static final int MAX_RETRIES = 3;
  
  private AwsTopologyDeploymentConf    conf;
  private AwsTopologyDeploymentContext context;
 
  @Mock
  private AmazonCloudFormation         cf;
  
  @Mock
  private AmazonEC2                    ec2;
 
  private MutableTime                  timeSupplier;
  
  private RetryCriteria                instancesRunningCheckRetry;
  
  private int                          expectedInstances;
  
  private WaitForInstancesStarted      step;

  @Before
  public void setUp() throws Exception {
    timeSupplier = MutableTime.getInstance();
    instancesRunningCheckRetry = RetryCriteria
        .forMaxAttempts(TimeMeasure.forSeconds(5).withTimeSupplier(timeSupplier), MAX_RETRIES)
        .withSleeper(Sleeper.NullSleeper.getInstance());
    
    step = new WaitForInstancesStarted();
    
    conf    = new AwsTopologyDeploymentConf();
    conf
      .withCorusCredentials(new CorusCredentials("test-app-id", "test-app-key"))
      .withInstancesRunningCheckRetry(instancesRunningCheckRetry);

    Topology topology = new Topology();
    topology.setApplication("app");
    topology.setOrg("org");
    topology.setVersion("1.0");
    
    Env env = new Env();
    env.setName("test");
    topology.addEnv(env);
    
    Cluster cluster = new Cluster();
    cluster.setName("test-cluster");
    env.addCluster(cluster);
    Machine m1 = new Machine();
    m1.setName("test-machine-1");
    m1.setMinInstances(2);
    cluster.addMachine(m1);
    Machine m2 = new Machine();
    m2.setName("test-machine-2");
    m2.setMinInstances(3);
    cluster.addMachine(m2);
    
    expectedInstances = m1.getMinInstances() + m2.getMinInstances();
    
    conf
      .withTopology(topology)
      .withEnvironment("test");
    
    context = new AwsTopologyDeploymentContext(conf, cf, ec2);
    context.assignStackId("test-stack");
  }

  @Test(expected = IllegalStateException.class)
  public void testExecute_insufficient_instances() throws Exception {
    
    doAnswer(new Answer<ListStackResourcesResult>() {
      @Override
      public ListStackResourcesResult answer(InvocationOnMock invocation) throws Throwable {
        return getStackResourceSummaries(expectedInstances);
      }
    }).when(cf).listStackResources(any(ListStackResourcesRequest.class));
    
    doAnswer(new Answer<DescribeInstanceStatusResult>() {
      @Override
      public DescribeInstanceStatusResult answer(InvocationOnMock invocation) throws Throwable {
        return getInstanceStatuses(expectedInstances - 2, InstanceStatusCode.RUNNING);
      }
    }).when(ec2).describeInstanceStatus(any(DescribeInstanceStatusRequest.class));
    
    step.execute(context);
  }
  
  @Test
  public void testExecute() throws Exception {
    
    doAnswer(new Answer<ListStackResourcesResult>() {
      @Override
      public ListStackResourcesResult answer(InvocationOnMock invocation) throws Throwable {
        return getStackResourceSummaries(expectedInstances);
      }
    }).when(cf).listStackResources(any(ListStackResourcesRequest.class));
    
    doAnswer(new Answer<DescribeInstanceStatusResult>() {
      int attemptCount = 0;
      @Override
      public DescribeInstanceStatusResult answer(InvocationOnMock invocation) throws Throwable {
        if (attemptCount == MAX_RETRIES - 1) {
          DescribeInstanceStatusResult result = getInstanceStatuses(expectedInstances, InstanceStatusCode.RUNNING);
          return result;
        } else {
          DescribeInstanceStatusResult result = getInstanceStatuses(expectedInstances - 2, InstanceStatusCode.RUNNING);
          attemptCount++;
          return result;
        }
      }
    }).when(ec2).describeInstanceStatus(any(DescribeInstanceStatusRequest.class));
    
    step.execute(context);
  }
  
  private ListStackResourcesResult getStackResourceSummaries(int numResources) {
    List<StackResourceSummary> summaries = new ArrayList<>(numResources);
    for (int i = 0; i < numResources; i++) {
      StackResourceSummary summary = new StackResourceSummary();
      summary.setPhysicalResourceId("instance-" + i);
      summary.setResourceType(WaitForInstancesStarted.RESOURCE_TYPE_EC2_INSTANCE);
      summaries.add(summary);
    }
    return new ListStackResourcesResult().withStackResourceSummaries(summaries);
  }
  
  private DescribeInstanceStatusResult getInstanceStatuses(int numResults, InstanceStatusCode statusCode) {
    List<InstanceStatus> statuses = new ArrayList<>(numResults);
    for (int i = 0; i < numResults; i++) {
      statuses.add(
          new InstanceStatus()
            .withInstanceId("instance-" + i)
            .withInstanceState(new InstanceState()
            .withCode(statusCode.value())));
    }
    return new DescribeInstanceStatusResult().withInstanceStatuses(statuses);
  }
}
