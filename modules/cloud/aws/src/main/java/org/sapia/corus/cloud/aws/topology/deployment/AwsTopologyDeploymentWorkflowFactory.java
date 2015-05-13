package org.sapia.corus.cloud.aws.topology.deployment;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.cloud.platform.workflow.Workflow;
import org.sapia.corus.cloud.platform.workflow.WorkflowImpl;
import org.sapia.corus.cloud.platform.workflow.WorkflowLog;
import org.sapia.corus.cloud.platform.workflow.WorkflowStep;

public class AwsTopologyDeploymentWorkflowFactory {

  private AwsTopologyDeploymentWorkflowFactory() {
  }
  
  /**
   * @param log the {@link WorkflowLog} to use for log output.
   * @return a new {@link Workflow}.
   */
  public static Workflow<AwsTopologyDeploymentContext> getDefaultWorkFlow(WorkflowLog log) {
    List<WorkflowStep<AwsTopologyDeploymentContext>> steps = new ArrayList<WorkflowStep<AwsTopologyDeploymentContext>>();
    steps.add(new GenerateCloudFormationFile());
    steps.add(new CreateStack());
    steps.add(new WaitForStackCreationCompleted());
    return new WorkflowImpl<AwsTopologyDeploymentContext>(log, steps);
  }

}
