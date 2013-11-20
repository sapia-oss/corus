package org.sapia.corus.deployer;

import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.ubik.util.Strings;

public class DeployerConfigurationImpl implements DeployerConfiguration {

  static final long serialVersionUID = 1L;

  public static final long DEFAULT_FILELOCK_TIMEOUT = 120000L;

  private String deployDir;
  private String tempDir;
  private String repoDir;
  private String scriptDir;
  private String uploadDir;
  private long fileLockTimeout = DEFAULT_FILELOCK_TIMEOUT;

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
  public void copyFrom(DeployerConfiguration other) {
    this.fileLockTimeout = other.getFileLockTimeout();
    this.deployDir = other.getDeployDir();
    this.repoDir = other.getRepoDir();
    this.scriptDir = other.getScriptDir();
    this.tempDir = other.getTempDir();
    this.uploadDir = other.getUploadDir();
  }

  @Override
  public String toString() {
    return Strings.toStringFor(this, "deployDir", deployDir, "repoDir", repoDir, "scriptDir", scriptDir, "tempDir", tempDir);
  }

}
