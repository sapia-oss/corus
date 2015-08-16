package org.sapia.corus.client.services.diagnostic;

/**
 * Holds constants corresponding to the different diagnostics at the process config level.
 * 
 * @author yduchesne
 *
 */
public enum ProcessConfigDiagnosticStatus {
  
  SUCCESS("All processes responding", true, false),
  FAILURE("One or more processes not responding", true, true),
  BUSY("System in a state preventing diagnostic acquisition at this time", false, false),

  NO_PROCESSES_EXPECTED("No process expected to be running at this time", true, false),
  NO_DIAGNOSTIC_AVAILABLE("No process has diagnostic configuration defined (this defeats diagnostic acquisition)", true, false),
  PENDING_EXECUTION("One or more processes pending execution", false, false),
  MISSING_PROCESS_INSTANCES("One or more processes are likely down", true, true);
  
  private boolean isFinal, isProblem;
  private String description;
  
  private ProcessConfigDiagnosticStatus(String description, boolean isFinal, boolean isProblem) {
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