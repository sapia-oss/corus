package org.sapia.corus.client.services.diagnostic;

public enum ProcessDiagnosticStatus {
  
  CHECK_SUCCESSFUL("Process checked successfully", true, false),
  CHECK_FAILED("Process check failed", true, true),
  STALE("Process is stale", true, true),
  SHUTTING_DOWN("Process is shutting down", false, false),
  RESTARTING("Process is restarting", false, false),
  SUSPECT("Process check failed, but might not have finished startup", false, false),
  NO_DIAGNOSTIC_CONFIG("Process has no diagnostic configuration defined", true, false),
  PROCESS_LOCKED("Process currently locked, try again", false,false),
  NO_ACTIVE_PORT("Process has no port active", true, false);
  
  private boolean isFinal, isProblem;
  private String description;
  
  private ProcessDiagnosticStatus(String description, boolean isFinal, boolean isProblem) {
    this.description = description;
    this.isFinal     = isFinal;
    this.isProblem   = isProblem;
  }
 
  /**
   * @return <code>true</code> if this status is "final" in the sense that no further action is required.
   */
  public boolean isFinal() {
    return isFinal;
  }
  
  /**
   * @return <code>true</code> if this status indicates a problem.
   */
  public boolean isProblem() {
    return isProblem;
  }
  
  /**
   * @return this instance's description.
   */
  public String description() {
    return description;
  }
}