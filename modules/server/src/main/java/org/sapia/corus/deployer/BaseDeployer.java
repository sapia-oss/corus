package org.sapia.corus.deployer;

import java.util.List;

import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.ProgressQueueImpl;
import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.exceptions.deployer.RunningProcessesException;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;

public class BaseDeployer implements Deployer{

  protected DeployerConfigurationImpl _configuration = new DeployerConfigurationImpl();
  private DistributionDatabase        _store         = new DistributionDatabaseImpl();
  
  public DeployerConfiguration getConfiguration() {
    return _configuration;
  }
  
  @Override
  public Distribution getDistribution(DistributionCriteria criteria)
      throws DistributionNotFoundException {
      return _store.getDistribution(criteria);
  }
  
  
  @Override
  public List<Distribution> getDistributions(DistributionCriteria criteria) {
    return _store.getDistributions(criteria);
  }
  
  @Override
  public ProgressQueue undeploy(DistributionCriteria criteria)
      throws RunningProcessesException {
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
