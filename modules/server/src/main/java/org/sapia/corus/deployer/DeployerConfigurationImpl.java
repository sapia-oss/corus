package org.sapia.corus.deployer;

import org.sapia.corus.client.services.deployer.DeployerConfiguration;

public class DeployerConfigurationImpl implements DeployerConfiguration{
  
  static final long serialVersionUID = 1L;

  public static final long DEFAULT_FILELOCK_TIMEOUT = 120000L;

  
  private String deployDir;
  private String tempDir;
  private long 	 fileLockTimeout = DEFAULT_FILELOCK_TIMEOUT;
  
  public String getDeployDir() {
    return deployDir;
  }
  
  public void setDeployDir(String deployDir) {
    this.deployDir = deployDir;
  }
 
  public long getFileLockTimeout() {
    return fileLockTimeout;
  }
  public void setFileLockTimeout(long fileLockTimeout) {
    this.fileLockTimeout = fileLockTimeout;
  }
  
  public String getTempDir() {
    return tempDir;
  }
  public void setTempDir(String tempDir) {
    this.tempDir = tempDir;
  }

}
