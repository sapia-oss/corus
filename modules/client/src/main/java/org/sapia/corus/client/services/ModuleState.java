package org.sapia.corus.client.services;

/**
 * Holds constants corresponding to the different states a Corus module can be in.
 * 
 * @author yduchesne
 *
 */
public enum ModuleState {

  IDLE("Indicates that no activity is currently occurring"),
  BUSY("Indicates that some activity is ongoing, preventing execution of some concurrent tasks, or probing of certain conditions");
  
  private String description;
  
  private ModuleState(String description) {
    this.description = description;
  }
  
  /**
   * @return this instance's description.
   */
  public String description() {
    return description;
  }

  /**
   * @return <code>true</code> if this instance corresponds to the {@link #BUSY} constant.
   */
  public boolean isBusy() {
    return this == BUSY;
  }
  
  /**
   * @return <code>true</code> if this instance corresponds to the {@link #IDLE} constant.
   */
  public boolean isIdle() {
    return this == IDLE;
  }
 
}
