package org.sapia.corus.cloud.platform.workflow;

/**
 * Specifies the behavior common to steps in a worflow.
 * 
 * @author yduchesne
 *
 * @param <C> the specific type of {@link WorkflowContext} handled by the step.
 */
public interface WorkflowStep<C extends WorkflowContext> {
  
  /**
   * @return this instance's human-readable description.
   */
  public String getDescription();

  /**
   * @param context the context holding data accessible by all the steps in the workflow.
   * @throws Exception if an error occurs while performing this step.
   */
  public void execute(C context) throws Exception;
}
