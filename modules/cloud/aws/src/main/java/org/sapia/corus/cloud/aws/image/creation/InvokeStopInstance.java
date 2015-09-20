package org.sapia.corus.cloud.aws.image.creation;

import org.sapia.corus.cloud.platform.workflow.WorkflowStep;

import com.amazonaws.services.ec2.model.StopInstancesRequest;

/**
 * Invokes stoppage of the instance used for creating the image.
 * 
 * @author yduchesne
 *
 */
public class InvokeStopInstance implements WorkflowStep<ImageCreationContext> {
  
  private static final String DESC = "Stopping instance used for image creation";
  
  @Override
  public String getDescription() {
    return DESC;
  }
  
  @Override
  public void execute(ImageCreationContext context) throws Exception {
    context.getEc2Client().stopInstances(new StopInstancesRequest().withInstanceIds(context.getAllocatedPublicIp()));
  }
}
