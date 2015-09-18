package org.sapia.corus.client.services.diagnostic;

/**
 * Holds constants corresponding to the different possible actions
 * following acquisition of a diagnostic.
 * <p>
 * Such actions are provided as a suggestion, in order to facilitate automation.
 * 
 * @author yduchesne
 *
 */
public enum SuggestionDiagnosticAction {

  NOOP("No further action required"),
  RETRY("Diagnosic incomplete, retry"),
  REMEDIATE("Problem detected, remediation should be considered");
  
  private String description;
  
  private SuggestionDiagnosticAction(String desc) {
    this.description = desc;
  }
  
  
  public String getDescription() {
    return description;
  }
}
