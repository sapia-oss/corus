package org.sapia.corus.client.services.deployer;

import java.rmi.Remote;

/**
 * Specifies Deployer configuration behavior.
 * 
 * @author yduchesne
 *
 */
public interface DeployerConfiguration extends Remote{

	/**
	 * @return the path to deployment directory.
	 */
  public String getDeployDir();

  /**
   * @return the timeout when locking deployment files.
   */
  public long getFileLockTimeout();

  /**
   * @return the deployment temporary directory.
   */
  public String getTempDir();

}