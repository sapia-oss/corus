package org.sapia.corus.cloud.aws.topology.deployment;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.cloud.aws.topology.deployment.WaitForStackCreationCompleted.StackCreationStatus;
import org.sapia.corus.cloud.platform.rest.CorusCredentials;
import org.sapia.corus.cloud.topology.Topology;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.DescribeStacksRequest;
import com.amazonaws.services.cloudformation.model.DescribeStacksResult;
import com.amazonaws.services.cloudformation.model.Stack;
import com.amazonaws.services.ec2.AmazonEC2;

@RunWith(MockitoJUnitRunner.class)
public class WaitForStackCreationCompletedTest {
  
  private AwsTopologyDeploymentConf    conf;
  private AwsTopologyDeploymentContext context;
 
  @Mock
  private AmazonCloudFormation         cf;
  
  @Mock
  private AmazonEC2                    ec2;
  
  private WaitForStackCreationCompleted waitForStack;

  @Before
  public void setUp() throws Exception {
    conf    = new AwsTopologyDeploymentConf();
    conf.withCorusCredentials(new CorusCredentials("test-app-id", "test-app-key"));

    Topology topology = new Topology();
    topology.setApplication("app");
    topology.setOrg("org");
    topology.setVersion("1.0");
    
    conf
      .withTopology(topology)
      .withEnvironment("test");
    
    context = new AwsTopologyDeploymentContext(conf, cf, ec2);
    context.assignStackId("test-stack");
    
    waitForStack = new WaitForStackCreationCompleted();
    
    DescribeStacksResult result = new DescribeStacksResult();
    Stack stack = new Stack();
    stack.withStackId("test-stack");
    stack.withStackStatus(StackCreationStatus.CREATE_COMPLETE.name());
    result.withStacks(stack);
    
    when(cf.describeStacks(any(DescribeStacksRequest.class))).thenReturn(result);
  }

  @Test
  public void testExecute() throws Exception {
    waitForStack.execute(context);
    verify(cf).describeStacks(any(DescribeStacksRequest.class));
  }

}
