package org.sapia.corus.deployer;

import java.util.List;

import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.ProgressQueueImpl;
import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.deployer.dist.Distribution;

public class BaseDeployer implements Deployer{

  protected DeployerConfigurationImpl _configuration = new DeployerConfigurationImpl();
  private DistributionDatabase        _store         = new DistributionDatabaseImpl();
  
  public DeployerConfiguration getConfiguration() {
    return _configuration;
  }
  
  public Distribution getDistribution(Arg name, Arg version)
      throws DistributionNotFoundException {
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
