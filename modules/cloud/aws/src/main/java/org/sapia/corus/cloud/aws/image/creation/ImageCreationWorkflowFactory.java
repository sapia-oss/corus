package org.sapia.corus.cloud.aws.image.creation;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.cloud.platform.workflow.DefaultWorkflowLog;
import org.sapia.corus.cloud.platform.workflow.Workflow;
import org.sapia.corus.cloud.platform.workflow.WorkflowImpl;
import org.sapia.corus.cloud.platform.workflow.WorkflowLog;
import org.sapia.corus.cloud.platform.workflow.WorkflowStep;

/**
 * Use this factory to create an image creation {@link Workflow}.
 * 
 * @author yduchesne
 *
 */
public class ImageCreationWorkflowFactory {
  
  private ImageCreationWorkflowFactory() {
  }
  
  /**
   * @param log the {@link WorkflowLog} to use for log output.
   * @return a new {@link Workflow}.
   */
  public static Workflow<ImageCreationContext> getDefaultWorkFlow(WorkflowLog log) {
    List<WorkflowStep<ImageCreationContext>> steps = new ArrayList<WorkflowStep<ImageCreationContext>>();
    steps.add(new ValidateRegion());
    steps.add(new AllocatePublicIp());
    steps.add(new InvokeRunInstance());
    steps.add(new WaitForInstanceRunning());
    steps.add(new AssociatePublicIp());
    steps.add(new WaitForCompletedCorusInstall());
    steps.add(new InvokeStopInstance());
    steps.add(new WaitForInstanceStopped());
    steps.add(new InvokeCreateImage());
    steps.add(new WaitForImageCreationCompleted());
    steps.add(new InvokeTerminateInstance());
    steps.add(new WaitForInstanceTerminated());
    steps.add(new DeallocatePublicIp());
    return new WorkflowImpl<ImageCreationContext>(log, steps);
  }

  /**
   * @return a new default image creation {@link Workflow}.
   */
  public static Workflow<ImageCreationContext> getDefaultWorkflow() {
    return getDefaultWorkFlow(DefaultWorkflowLog.getDefault());
  }
}
