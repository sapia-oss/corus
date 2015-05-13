package org.sapia.corus.cloud.platform.workflow;

import java.util.List;

/**
 * Specifies workflow behavior: a workflow consists of a series of well-identified steps.
 * 
 * @author yduchesne
 *
 * @param <C> a generic specifying the type of {@link WorkflowContext} on which the implementation depends.
 */
public interface Workflow<C extends WorkflowContext> {
  
  /**
   * Holds constants corresponding to the different workflow phases. Two phases are identified: the "normal" phase corresponds 
   * a workflow's normal execution, that is: when no error has yet occurred. In most cases (hopefully), no error should ever
   * occur, therefore worklflow are being executed "normally" most of the time.
   * <p>
   * If one of the {@link WorkflowStep}s throws an {@link Exception}, the steps whose class implements the {@link GuardedExecutionCapable}
   * interface will then be executed, provided their prerequisite steps have been executed successfully beforehand. The phase 
   * corresponding to the execution of steps after an {@link Exception} has been thrown is the "cleanup" phase. This allows
   * phases executed in the context of that phase to perform cleanup (as in a <code>finally</code> block).
   * 
   * @author yduchesne
   *
   */
  public enum Phase {
    
    /**
     * Indicates that a {@link WorkflowStep} was executed during the normal course of a workflow, that is: 
     * before an {@link Exception} was thrown by one of the steps (if indeed such an exception was thrown). 
     */
    NORMAL,
    
    /**
     * Indicates that a {@link WorkflowStep} was executed during the cleanup phase of a workflow, that is:
     * after an {@link Exception} was thrown by one of the previous steps.
     */
    CLEANUP;
  }
  
  // --------------------------------------------------------------------------
  
  /**
   * Provides metadata about a workflow step.
   * 
   * @author yduchesne
   *
   */
  public static class StepDescriptor {
    
    private String stepName, stepDescription;
    
    public StepDescriptor(String name, String desc) {
      this.stepName        = name;
      this.stepDescription = desc;
    }
    
    public String getStepName() {
      return stepName;
    }
    
    public String getStepDescription() {
      return stepDescription;
    }
    
  }
  
  // --------------------------------------------------------------------------
  
  /**
   * @return the {@link List} of {@link StepDescriptor}s corresponding to the steps
   * that this workflow encapsulates.
   */
  public List<StepDescriptor> getStepDescriptors();
  
  /**
   * 
   * @param context {@link WorkflowContext} to process.
   * @throws Exception if an error occurs while processing.
   */
  public void execute(C context);
  
  /**
   * @return the {@link WorkflowResult} encapsulating data about the outcome of this instance's execution.
   */
  public WorkflowResult getResult();
}
