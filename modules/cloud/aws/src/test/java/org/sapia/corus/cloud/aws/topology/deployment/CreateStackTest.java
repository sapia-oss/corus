package org.sapia.corus.cloud.aws.topology.deployment;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.cloud.topology.Topology;

import com.amazonaws.services.cloudformation.AmazonCloudFormation;
import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.CreateStackResult;
import com.amazonaws.services.ec2.AmazonEC2;

@RunWith(MockitoJUnitRunner.class)
public class CreateStackTest {

  private AwsTopologyDeploymentConf    conf;
  private AwsTopologyDeploymentContext context;
 
  @Mock
  private AmazonCloudFormation         cf;
  
  @Mock
  private AmazonEC2                    ec2;
  
  private Topology                     topology;
  private CreateStack                  createStack;
  
  @Before
  public void setUp() throws Exception {
    conf     = new AwsTopologyDeploymentConf();
    
    topology = new Topology();
    topology.setApplication("app");
    topology.setOrg("org");
    topology.setVersion("1.0");
    
    conf
      .withTopology(topology)
      .withEnvironment("test");
    
    context  = new AwsTopologyDeploymentContext(conf, cf, ec2);
    
    createStack = new CreateStack() {
      protected String readCloudFormationFor(AwsTopologyDeploymentContext context) throws java.io.IOException {
        return "test";
      }
    };
    
    CreateStackResult result = new CreateStackResult();
    result.setStackId("test-stack");
    when(cf.createStack(any(CreateStackRequest.class))).thenReturn(result);
  }

  @Test
  public void testExecute() throws Exception {
    createStack.execute(context);
    assertEquals("test-stack", context.getStackId());
  }

}
