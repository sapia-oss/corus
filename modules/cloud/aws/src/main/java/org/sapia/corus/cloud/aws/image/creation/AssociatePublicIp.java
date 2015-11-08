package org.sapia.corus.cloud.aws.image.creation;

import org.sapia.corus.cloud.platform.workflow.WorkflowStep;

import com.amazonaws.services.ec2.model.AssociateAddressRequest;

/**
 * Associates a previously allocated EIP to the just launched instance.
 * 
 * @author yduchesne
 *
 */
public class AssociatePublicIp implements WorkflowStep<ImageCreationContext> {
  
  private static final String DESC = "associating allocated Elastic IP to instance used for image creation";
  
  @Override
  public String getDescription() {
    return DESC;
  }
  
  @Override
  public void execute(ImageCreationContext context) throws Exception {
    context.getEc2Client().associateAddress(new AssociateAddressRequest(context.getStartedInstanceId(), context.getAllocatedPublicIp()));
  }

}
