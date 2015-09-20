package org.sapia.corus.cloud.platform.workflow;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.sapia.corus.cloud.aws.util.TimeMeasure;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * An instance of this class provides data pertaining to the result of a {@link Workflow}'s
 * execution: it internally holds a {@link WorkflowStepResult} for each step that was
 * executed as part of the workflow.
 * <p>
 * An {@link WorkflowResult} will have a {@link Outcome#FAILURE} outcome if at least one workflow step
 * resulted in an error (that is, if one if the {@link WorkflowStepResult} encapsulates an {@link Exception}).
 * 
 * @author yduchesne
 *
 */
public class WorkflowResult {
  
  /**
   * Determines if a workflow's execution was successful, or deemed a failure.
   * 
   * @author yduchesne
   *
   */
  public enum Outcome {
    
    /**
     * This constant indicates that all steps in the workflow were executed successfully..
     */
    SUCCESS, 
    
    /**
     * This constant indicates that at least one step in the workflow resulted in an error.
     */
    FAILURE;
  }
  
  // --------------------------------------------------------------------------
  
  private WorkflowResult.Outcome outcome;
  private List<WorkflowStepResult> stepResults;
  private TimeMeasure duration;
  
  /**
   * @param outcome the {@link Outcome}.
   * @param stepResults the {@link List} of {@link WorkflowStepResult}s corresponding to the 
   * steps that were executed as part of the workflow.
   * @param duration a {@link TimeMeasure} corresponding to the time that the workflow's execution took.
   */
  public WorkflowResult(WorkflowResult.Outcome outcome, List<WorkflowStepResult> stepResults, TimeMeasure duration) {
    this.outcome     = outcome;
    this.stepResults = stepResults;
    this.duration    = duration;
  }
  
  /**
   * @return the time that the workflow's execution took.
   */
  public TimeMeasure getDuration() {
    return duration;
  }
  
  /**
   * @return the {@link Collection} of {@link WorkflowStepResult}s corresponding to the steps 
   * whose execution resulted in an error.
   */
  public Collection<WorkflowStepResult> getFailureStepResults() {
    return Collections2.filter(stepResults, new Predicate<WorkflowStepResult>() {
      @Override
      public boolean apply(WorkflowStepResult input) {
        return input.isError();
      }
    });
  }
  
  /**
   * @return the {@link Collection} of {@link WorkflowStepResult}s corresponding to the steps 
   * whose execution complete successfully.
   */
  public Collection<WorkflowStepResult> getSuccessStepResults() {
    return Collections2.filter(stepResults, new Predicate<WorkflowStepResult>() {
      @Override
      public boolean apply(WorkflowStepResult input) {
        return input.isSuccess();
      }
    });
  }
  
  /**
   * @return the {@link Collection} of all {@link WorkflowStepResult}s, whether the execution of the 
   * step resulted in an error or not.
   */
  public Collection<WorkflowStepResult> getStepResults() {
    return Collections.unmodifiableList(stepResults);
  }
  
  /**
   * @return the outcome of the workflow's execution.
   */
  public WorkflowResult.Outcome getOutcome() {
    return outcome;
  }
}