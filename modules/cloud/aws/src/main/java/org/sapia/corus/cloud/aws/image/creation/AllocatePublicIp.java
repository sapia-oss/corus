package org.sapia.corus.cloud.aws.image.creation;

import org.sapia.corus.cloud.platform.workflow.WorkflowStep;

import com.amazonaws.services.ec2.model.AllocateAddressRequest;
import com.amazonaws.services.ec2.model.AllocateAddressResult;
import com.amazonaws.services.ec2.model.DomainType;

/**
 * Allocates an Elastic IP for the instance that will be launched.
 * 
 * @author yduchesne
 *
 */
public class AllocatePublicIp implements WorkflowStep<ImageCreationContext> {

  private static final String DESC = "requesting allocation of Elastic IP to instance used for image creation";
  
  @Override
  public String getDescription() {
    return DESC;
  }
  
  @Override
  public void execute(ImageCreationContext context) {
    AllocateAddressRequest allocateReq = new AllocateAddressRequest();
    allocateReq.withDomain(DomainType.Vpc);
    AllocateAddressResult allocateRes = context.getEc2Client().allocateAddress(allocateReq);
    context.assignAllocatedPublicIp(allocateRes.getPublicIp(), allocateRes.getAllocationId());
  }

}
