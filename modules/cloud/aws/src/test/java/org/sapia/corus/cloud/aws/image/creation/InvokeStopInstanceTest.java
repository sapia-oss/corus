package org.sapia.corus.cloud.aws.image.creation;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;

public class InvokeStopInstanceTest {
  
  private ImageCreationContext context;
  private InvokeStopInstance   step;
  
  @Before
  public void setUp() {
    step    = new InvokeStopInstance();
    context = ImageCreationTestHelper.createContext();
    context.assignStartedInstanceId("testInstanceId");
    context.assignAllocatedPublicIp("testPublicIp", "testAllocId");
    
    when(context.getEc2Client().stopInstances(any(StopInstancesRequest.class))).thenReturn(new StopInstancesResult());
  }

  @Test
  public void testExecute() throws Exception {
    step.execute(context);
    verify(context.getEc2Client()).stopInstances(any(StopInstancesRequest.class));
  }

}
