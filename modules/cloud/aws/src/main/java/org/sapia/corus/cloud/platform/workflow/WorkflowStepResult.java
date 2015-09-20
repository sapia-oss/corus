package org.sapia.corus.cloud.platform.workflow;

import org.sapia.corus.cloud.aws.util.TimeMeasure;
import org.sapia.corus.cloud.platform.workflow.Workflow.Phase;
import org.sapia.corus.cloud.platform.workflow.Workflow.StepDescriptor;

/**
 * Provides details about a workflow step's execution.
 * 
 * @author yduchesne
 *
 */
public class WorkflowStepResult {
  
  private Workflow.StepDescriptor step;
  private Exception               error;
  private Workflow.Phase          phase;
  private TimeMeasure             execDuration;

  /**
   * @param step the {@link StepDescriptor} corresponding to the {@link WorkflowStep} that was executed.
   * @param phase the {@link Phase} in the worflow's execution in the context of which the {@link WorkflowStep}
   * was executed.
   * @param execDuration a {@link TimeMeasure} corresponding to the workflow step's execution duration.
   */
  public WorkflowStepResult(Workflow.StepDescriptor step, Workflow.Phase phase, TimeMeasure execDuration) {
    this(step, null, phase, execDuration);
  }

  /**
   * @param step the {@link StepDescriptor} corresponding to the {@link WorkflowStep} that was executed.
   * @param err the {@link Exception} corresponding that was thrown by the {@link WorkflowStep}.
   * @param phase the {@link Phase} in the worflow's execution in the context of which the {@link WorkflowStep}
   * was executed.
   * @param execDuration a {@link TimeMeasure} corresponding to the workflow step's execution duration.
   */
  public WorkflowStepResult(Workflow.StepDescriptor step, Exception err, Workflow.Phase phase, TimeMeasure execDuration) {
    this.step  = step;
    this.error = err;
    this.phase = phase;
    this.execDuration = execDuration;
  }
  
  /**
   * @return the {@link Exception} that occurred.
   * @throws IllegalStateException if this instance has no error set.
   */
  public Exception getError() throws IllegalStateException {
    if (error == null) throw new IllegalStateException("Workflow step did not result in an error");
    return error;
  }
  
  /**
   * @return <code>true</code> if this instance encapsulates an {@link Exception},
   * corresponding to an execution error.
   */
  public boolean isError() {
    return error != null;
  }
  
  /**
   * @return <code>true</code> if this instance DOES NOT encapsulate an {@link Exception}.
   * @see #isError()
   */
  public boolean isSuccess() {
    return !isError();
  }
  
  /**
   * @return the workflow phase in the context of which the exception occurred.
   */
  public Workflow.Phase getPhase() {
    return phase;
  }
  
  /**
   * @return the {@link StepDescriptor} corresponding to the workflow step in
   * the context of which the error happened.
   */
  public Workflow.StepDescriptor getStep() {
    return step;
  }   
  
  /**
   * @return the time taken by the execution of this instance's corresponding workflow step.
   */
  public TimeMeasure getExecDuration() {
    return execDuration;
  }
  
}