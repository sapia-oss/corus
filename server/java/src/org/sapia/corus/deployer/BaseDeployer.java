package org.sapia.corus.deployer;

import java.util.List;

import org.sapia.corus.admin.Arg;
import org.sapia.corus.admin.services.deployer.Deployer;
import org.sapia.corus.admin.services.deployer.DeployerConfiguration;
import org.sapia.corus.admin.services.deployer.DeployerConfigurationImpl;
import org.sapia.corus.admin.services.deployer.dist.Distribution;
import org.sapia.corus.exceptions.LogicException;
import org.sapia.corus.util.ProgressQueue;
import org.sapia.corus.util.ProgressQueueImpl;

public class BaseDeployer implements Deployer{

  protected DeployerConfigurationImpl _configuration = new DeployerConfigurationImpl();
  private DistributionDatabase        _store         = new DistributionDatabaseImpl();
  
  public DeployerConfiguration getConfiguration() {
    return _configuration;
  }
  
  public Distribution getDistribution(Arg name, Arg version)
      throws LogicException {
    return _store.getDistribution(name, version);
  }
  
  public List<Distribution> getDistributions() {
    return _store.getDistributions();
  }
  
  public List<Distribution> getDistributions(Arg name) {
    return _store.getDistributions(name);
  }
  
  public List<Distribution> getDistributions(Arg name, Arg version) {
    return _store.getDistributions(name, version);
  }
  
  public ProgressQueue undeploy(Arg distName, Arg version) {
    ProgressQueue q = new ProgressQueueImpl();
    q.close();
    return q;
  }
  
  public String getRoleName() {
    return Deployer.ROLE;
  }
  
  protected DistributionDatabase getDistributionDatabase(){
    return _store;
  }
  
}
