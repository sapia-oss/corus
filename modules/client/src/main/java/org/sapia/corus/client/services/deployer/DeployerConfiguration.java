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

  /**
   * @return the path to the repo directory.
   */
  public String getRepoDir();
  
  /**
   * @return the path to the directory where shell script files are kept.
   */
  public String getScriptDir();

  /**
   * @return the path to the upload directory (where arbitrary files are kept).
   */
  public String getUploadDir();
  
  /**
   * Copies the given configuration's values to this instance.
   * 
   * @param other another {@link DeployerConfiguration}.
   */
  public void copyFrom(DeployerConfiguration other);
}