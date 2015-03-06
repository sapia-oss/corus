package org.sapia.corus.client.services.deployer;

import java.io.Serializable;

import org.sapia.ubik.util.Strings;

/**
 * Holds different deployment-related flags.
 * 
 * @author yduchesne
 *
 */
public class DeployPreferences implements Serializable {

  static final long serialVersionUID = 1L;
  
  private boolean execDeployScripts;
  
  /**
   * Indicates that the deploy scripts in the distribution should be executed.
   * 
   * @return this instance.
   */
  public DeployPreferences executeDeployScripts() {
    this.execDeployScripts = true;
    return this;
  }
  
  /**
   * @param execDeployScripts if <code>true</code>, indicates that the deploy scripts in the distribution 
   * should be executed.
   * @return this instance.
   */
  public DeployPreferences setExecDeployScripts(boolean execDeployScripts) {
    this.execDeployScripts = execDeployScripts;
    return this;
  }
  
  /**
   * @return <code>true</code> if the deploy scripts should be executed.
   */
  public boolean isExecuteDeployScripts() {
    return execDeployScripts;
  }
  
  /**
   * @return a new instance of this class.
   */
  public static DeployPreferences newInstance() {
    return new DeployPreferences();
  }
  
  @Override
  public String toString() {
    return Strings.toString("execDeployScripts", execDeployScripts);
  }
  
}
