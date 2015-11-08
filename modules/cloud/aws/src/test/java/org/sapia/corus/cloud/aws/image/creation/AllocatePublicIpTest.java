package org.sapia.corus.cloud.aws.image.creation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.ec2.model.AllocateAddressRequest;
import com.amazonaws.services.ec2.model.AllocateAddressResult;


public class AllocatePublicIpTest {
  
  private ImageCreationContext context;
  private AllocatePublicIp     step;
  
  @Before
  public void setUp() {
    context = ImageCreationTestHelper.createContext();
    step = new AllocatePublicIp();
    when(context.getEc2Client().allocateAddress(any(AllocateAddressRequest.class)))
      .thenReturn(new AllocateAddressResult().withAllocationId("testAllocId").withPublicIp("testPublicIp"));
    
  }

  @Test
  public void testExecute() {
    step.execute(context);
    assertEquals("testPublicIp", context.getAllocatedPublicIp());
    assertEquals("testAllocId", context.getIpAllocationId());
  }

}
