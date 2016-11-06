package org.sapia.corus.client.services.deployer.event;

import org.sapia.ubik.rmi.interceptor.Event;

public class DeploymentScriptExecutedEvent implements Event {
  
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
