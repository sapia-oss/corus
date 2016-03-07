package org.sapia.corus.cloud.platform.domain;

/**
 * An entry in a {@link DeploymentJournal}.
 * 
 * @author yduchesne
 *
 */
public class DeploymentJournalEntry {
  
  public enum DeploymentStatus {
    
    SUCCESS, FAILURE, PARTIAL_SUCCESS;
    
  }
  
  private CorusInstance corusInstance;
  private DeploymentStatus  status;
  private String message;

  public DeploymentJournalEntry(CorusInstance instance, DeploymentStatus status, String message) {
    this.corusInstance = instance;
    this.status        = status;
    this.message       = message;
  }
  
  public CorusInstance getCorusInstance() {
    return corusInstance;
  }
  
  public DeploymentStatus getStatus() {
    return status;
  }
  
  public String getMessage() {
    return message;
  }
}
