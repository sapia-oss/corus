package org.sapia.corus.client.services.deployer;

import java.rmi.Remote;


public interface DeployerConfiguration extends Remote{

  public abstract String getDeployDir();

  public abstract long getFileLockTimeout();

  public abstract String getTempDir();

}