package org.sapia.corus.cloud.aws.image.creation;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.ec2.model.ReleaseAddressRequest;

public class DeallocatePublicIpTest {
  
  private ImageCreationContext context;
  private DeallocatePublicIp   step;
  
  @Before
  public void setUp() {
    context = ImageCreationTestHelper.createContext();
    context.assignAllocatedPublicIp("testPublicIp", "testAllocId");
    step    = new DeallocatePublicIp();
  }

  @Test
  public void testExecute() throws Exception {
    step.execute(context);
    verify(context.getEc2Client()).releaseAddress(any(ReleaseAddressRequest.class));   
  }

}
