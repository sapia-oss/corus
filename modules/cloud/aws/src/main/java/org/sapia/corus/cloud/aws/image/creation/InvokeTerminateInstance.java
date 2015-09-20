package org.sapia.corus.cloud.aws.image.creation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.sapia.corus.cloud.platform.workflow.GuardedExecutionCapable;
import org.sapia.corus.cloud.platform.workflow.WorkflowStep;

import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.google.common.collect.Lists;

/**
 * Terminates the instance from which an image was created.
 * 
 * @author yduchesne
 *
 */
public class InvokeTerminateInstance implements WorkflowStep<ImageCreationContext>, GuardedExecutionCapable {
  
  private static final String DESC = "Terminating instance from which an image was created";
  
  private static final Set<Class<?>> PRE_REQS = new HashSet<Class<?>>();
  {
    PRE_REQS.add(InvokeRunInstance.class);
  }
  
  @Override
  public String getDescription() {
    return DESC;
  }
  
  @Override
  public void execute(ImageCreationContext context) throws Exception {
    TerminateInstancesRequest terminateReq = new TerminateInstancesRequest(Lists.newArrayList(context.getStartedInstanceId()));
    context.getEc2Client().terminateInstances(terminateReq);
  }

  @Override
  public Set<Class<?>> getGuardedExecutionPrerequisites() {
    return Collections.unmodifiableSet(PRE_REQS);
  }

}
