package org.sapia.corus.client.services.diagnostic;

/**
 * Holds constants corresponding to the different system diagnostic statuses.
 * 
 * @author yduchesne
 *
 */
public enum SystemDiagnosticStatus {
  
  UP("Currently operational"),
  DOWN("Currently down"),
  BUSY("Currently busy (temporary state)");
  
  private String defaultMessage;
  
  private SystemDiagnosticStatus(String msg) {
    defaultMessage = msg;
  }
  
  /**
   * @return the {@link GlobalDiagnosticStatus} to which this instance corresponds.
   */
  public GlobalDiagnosticStatus getMatchingGlobalDiagnostic() {
    switch (this) {
      case UP:
         return GlobalDiagnosticStatus.SUCCESS;
      case DOWN:
          return GlobalDiagnosticStatus.FAILURE;
      case BUSY:
          return GlobalDiagnosticStatus.INCOMPLETE;
      default:
          throw new IllegalStateException("System diagnostic status could not be match to a global diagnostic status: " + this.name());
    }
  }
  
  public String getDefaultMessage() {
    return defaultMessage;
  }
 

}
