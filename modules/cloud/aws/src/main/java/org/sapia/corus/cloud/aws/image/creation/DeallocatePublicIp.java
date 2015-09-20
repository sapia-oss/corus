package org.sapia.corus.cloud.aws.image.creation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.sapia.corus.cloud.platform.workflow.GuardedExecutionCapable;
import org.sapia.corus.cloud.platform.workflow.WorkflowStep;

import com.amazonaws.services.ec2.model.ReleaseAddressRequest;

/**
 * Deallocates the EIP that was previously allocated.
 * 
 * @author yduchesne
 *
 */
public class DeallocatePublicIp implements WorkflowStep<ImageCreationContext>, GuardedExecutionCapable {
  
  private static final String DESC = "Deallocating elastic IP used to connect to instance";
  private static final Set<Class<?>> PRE_REQS = new HashSet<Class<?>>();
  {
    PRE_REQS.add(AllocatePublicIp.class);
  }
  
  @Override
  public String getDescription() {
    return DESC;
  }
  
  @Override
  public void execute(ImageCreationContext context) throws Exception {
    context.getEc2Client().releaseAddress(new ReleaseAddressRequest().withAllocationId(context.getIpAllocationId()));
  }
  
  @Override
  public Set<Class<?>> getGuardedExecutionPrerequisites() {
    return Collections.unmodifiableSet(PRE_REQS);
  }

}
