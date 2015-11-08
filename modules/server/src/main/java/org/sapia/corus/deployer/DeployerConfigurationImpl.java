package org.sapia.corus.deployer;

import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.ubik.util.Strings;

public class DeployerConfigurationImpl implements DeployerConfiguration {

  static final long serialVersionUID = 1L;

  public static final long DEFAULT_FILELOCK_TIMEOUT = 120000L;
  public static final int DEFAULT_TEMP_FILE_TIMEOUT_HOURS = 1;

  private String deployDir;
  private String tempDir;
  private String repoDir;
  private String scriptDir;
  private String uploadDir;
  private String archiveDir;
  private long fileLockTimeout = DEFAULT_FILELOCK_TIMEOUT;
  private int tempFileTimeoutHours = DEFAULT_TEMP_FILE_TIMEOUT_HOURS;

  @Override
  public String getDeployDir() {
    return deployDir;
  }

  public void setDeployDir(String deployDir) {
    this.deployDir = deployDir;
  }

  @Override
  public long getFileLockTimeout() {
    return fileLockTimeout;
  }

  public void setFileLockTimeout(long fileLockTimeout) {
    this.fileLockTimeout = fileLockTimeout;
  }

  @Override
  public String getTempDir() {
    return tempDir;
  }

  public void setTempDir(String tempDir) {
    this.tempDir = tempDir;
  }

  @Override
  public String getRepoDir() {
    return repoDir;
  }

  public void setRepoDir(String repoDir) {
    this.repoDir = repoDir;
  }

  @Override
  public String getArchiveDir() {
    return archiveDir;
  }
  
  public void setArchiveDir(String archiveDir) {
    this.archiveDir = archiveDir;
  }
  
  @Override
  public String getScriptDir() {
    return scriptDir;
  }

  public void setScriptDir(String scriptDir) {
    this.scriptDir = scriptDir;
  }

  @Override
  public String getUploadDir() {
    return uploadDir;
  }

  public void setUploadDir(String uploadDir) {
    this.uploadDir = uploadDir;
  }
  
  @Override
  public int getTempFileTimeoutHours() {
    return tempFileTimeoutHours;
  }
  
  public void setTempFileTimeoutHours(int tempFileTimeoutHours) {
    this.tempFileTimeoutHours = tempFileTimeoutHours;
  }

  @Override
  public void copyFrom(DeployerConfiguration other) {
    this.fileLockTimeout = other.getFileLockTimeout();
    this.deployDir = other.getDeployDir();
    this.repoDir = other.getRepoDir();
    this.scriptDir = other.getScriptDir();
    this.tempDir = other.getTempDir();
    this.uploadDir = other.getUploadDir();
    this.archiveDir = other.getArchiveDir();
  }

  @Override
  public String toString() {
    return Strings.toStringFor(this, 
        "deployDir", deployDir, 
        "repoDir", repoDir, 
        "scriptDir", scriptDir, 
        "tempDir", tempDir, 
        "archiveDir", archiveDir
    );
  }

}
