package org.sapia.corus.client.services.deployer.event;

public class DeploymentScriptExecutedEvent {
  
  private String scriptName;
  
  public DeploymentScriptExecutedEvent(String scriptName) {
    this.scriptName = scriptName;
  }
  
  /**
   * @return the name of the script that was executed.
   */
  public String getScriptName() {
    return scriptName;
  }

}
