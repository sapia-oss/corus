package org.sapia.corus.admin.services.deployer;

import java.io.Serializable;

import org.sapia.corus.util.LongProperty;
import org.sapia.corus.util.Property;

public class DeployerConfigurationImpl implements Serializable, DeployerConfiguration{
  
  static final long serialVersionUID = 1L;

  public static final Property DEFAULT_FILELOCK_TIMEOUT = new LongProperty(120000L);

  
  private Property deployDir;
  private Property tempDir;
  private Property fileLockTimeout = DEFAULT_FILELOCK_TIMEOUT;
  
  /* (non-Javadoc)
   * @see org.sapia.corus.admin.services.deployer.DeployerConfiguration#getDeployDir()
   */
  public Property getDeployDir() {
    return deployDir;
  }
  public void setDeployDir(Property deployDir) {
    this.deployDir = deployDir;
  }
 
  /* (non-Javadoc)
   * @see org.sapia.corus.admin.services.deployer.DeployerConfiguration#getFileLockTimeout()
   */
  public Property getFileLockTimeout() {
    return fileLockTimeout;
  }
  public void setFileLockTimeout(Property fileLockTimeout) {
    this.fileLockTimeout = fileLockTimeout;
  }
  
  /* (non-Javadoc)
   * @see org.sapia.corus.admin.services.deployer.DeployerConfiguration#getTempDir()
   */
  public Property getTempDir() {
    return tempDir;
  }
  public void setTempDir(Property tempDir) {
    this.tempDir = tempDir;
  }

}
