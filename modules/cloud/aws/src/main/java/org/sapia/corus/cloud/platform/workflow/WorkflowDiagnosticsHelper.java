package org.sapia.corus.cloud.platform.workflow;

import org.sapia.corus.cloud.platform.workflow.WorkflowResult.Outcome;

/**
 * Outputs workflow execution diagnostic information to a given {@link WorkflowLog}.
 * 
 * @author yduchesne
 *
 */
public class WorkflowDiagnosticsHelper {

  private static final int DEFAULT_LINE_WIDTH = 80;
  
  private WorkflowLog log = DefaultWorkflowLog.getDefault();
  private int lineWidth   = DEFAULT_LINE_WIDTH;
  
  /**
   * @param log {@link WorkflowLog} the {@link WorkflowLog} to output to.
   * @return {@link WorkflowDiagnosticsHelper} this instance.
   */
  public WorkflowDiagnosticsHelper withLog(WorkflowLog log) {
    this.log = log;
    return this;
  }
  
  /**
   * @param lineWidth the line width to use for display.
   * @return this instance.
   */
  public WorkflowDiagnosticsHelper withLineWidth(int lineWidth) {
    this.lineWidth = lineWidth;
    return this;
  }
  
  /**
   * @param result the {@link WorkflowResult} for which do display diagnostics.
   */
  public void displayDiagnostics(WorkflowResult result) {
    log.info("");
    log.info(repeat('=', lineWidth));
    log.info("");

    if (result.getOutcome() == Outcome.FAILURE) {
      log.info("Execution FAILED - took: " + result.getDuration().approximate().toLiteral());
    } else {
      log.info("Execution SUCCEEDED - took: " + result.getDuration().approximate().toLiteral());
    }
    log.info("");
    
    log.info("Execution step details:");
    log.info(repeat('-', lineWidth));
    
    for (WorkflowStepResult sr : result.getStepResults()) {
      log.info("=> %s (%s) " + (sr.isError() ? " - FAILED - " : " - SUCCEEDED - ") + " Phase: %s. Duration: %s", 
          sr.getStep().getStepName(), sr.getStep().getStepDescription(), sr.getPhase(), sr.getExecDuration().approximate().toLiteral());
      if (sr.isError()) {
        log.info("  => Error:");
        log.info(sr.getError());
      }
    }
    log.info("");
    log.info(repeat('=', lineWidth));
  }
  
  private String repeat(char c, int repetitions) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < repetitions; i++) {
      sb.append(c);
    }
    return sb.toString();
  }
  
}
