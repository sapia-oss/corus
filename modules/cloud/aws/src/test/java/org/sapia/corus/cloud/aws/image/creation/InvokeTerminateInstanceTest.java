package org.sapia.corus.cloud.aws.image.creation;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;

public class InvokeTerminateInstanceTest {
  
  private ImageCreationContext    context;
  private InvokeTerminateInstance step;
  
  @Before
  public void setUp() {
    step    = new InvokeTerminateInstance();
    context = ImageCreationTestHelper.createContext();
    context.assignStartedInstanceId("testInstanceId");
    context.assignAllocatedPublicIp("testPublicIp", "testAllocId");
    
    when(context.getEc2Client().terminateInstances(any(TerminateInstancesRequest.class))).thenReturn(new TerminateInstancesResult());
  }

  @Test
  public void testExecute() throws Exception {
    step.execute(context);
    verify(context.getEc2Client()).terminateInstances(any(TerminateInstancesRequest.class));
  }

}
