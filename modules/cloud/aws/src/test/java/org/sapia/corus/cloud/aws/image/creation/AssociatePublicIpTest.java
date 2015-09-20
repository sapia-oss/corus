package org.sapia.corus.cloud.aws.image.creation;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.ec2.model.AssociateAddressRequest;
import com.amazonaws.services.ec2.model.AssociateAddressResult;

public class AssociatePublicIpTest {

  private ImageCreationContext context;
  private AssociatePublicIp    step;
  
  @Before
  public void setUp() {
    context = ImageCreationTestHelper.createContext();
    context.assignStartedInstanceId("testInstanceId");
    context.assignAllocatedPublicIp("testPublicIp", "testAllocId");
    step = new AssociatePublicIp();
    when(context.getEc2Client().associateAddress(any(AssociateAddressRequest.class)))
      .thenReturn(new AssociateAddressResult().withAssociationId("testAssoId"));    
    
  }
  
  @Test
  public void testExecute() throws Exception {
    step.execute(context);
    verify(context.getEc2Client()).associateAddress(any(AssociateAddressRequest.class));
  }

}
