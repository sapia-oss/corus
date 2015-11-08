package org.sapia.corus.cloud.aws.image.creation;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.ec2.model.CreateImageRequest;
import com.amazonaws.services.ec2.model.CreateImageResult;

public class InvokeCreateImageTest {
  
  private ImageCreationContext context;
  private InvokeCreateImage    step;
  
  @Before
  public void setUp() {
    step = new InvokeCreateImage();
    context = ImageCreationTestHelper.createContext();
    context.assignStartedInstanceId("testInstanceId");
    context.assignAllocatedPublicIp("testPublicIp", "testAllocId");
    
    when(context.getEc2Client().createImage(any(CreateImageRequest.class))).thenReturn(new CreateImageResult().withImageId("testImageId"));
  }

  @Test
  public void testExecute() throws Exception {
    step.execute(context);
    assertEquals("testImageId", context.getCreatedImageId());
    
  }

}
