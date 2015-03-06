package org.sapia.corus.deployer;

import java.util.List;

import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.ProgressQueueImpl;
import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.exceptions.deployer.RollbackScriptNotFoundException;
import org.sapia.corus.client.exceptions.deployer.RunningProcessesException;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;

/**
 * A base {@link Deployer} implementation.
 * 
 * @author yduchesne
 * 
 */
public class BaseDeployer implements Deployer {

  protected DeployerConfigurationImpl configuration = new DeployerConfigurationImpl();
  private DistributionDatabase store = new DistributionDatabaseImpl();

  public DeployerConfiguration getConfiguration() {
    return configuration;
  }

  @Override
  public Distribution getDistribution(DistributionCriteria criteria) throws DistributionNotFoundException {
    return store.getDistribution(criteria);
  }

  @Override
  public List<Distribution> getDistributions(DistributionCriteria criteria) {
    return store.getDistributions(criteria);
  }

  @Override
  public ProgressQueue undeploy(DistributionCriteria criteria) throws RunningProcessesException {
    ProgressQueue q = new ProgressQueueImpl();
    q.close();
    return q;
  }
  
  @Override
  public ProgressQueue rollbackDistribution(String name, String version)
      throws RollbackScriptNotFoundException, DistributionNotFoundException {
    ProgressQueue q = new ProgressQueueImpl();
    q.close();
    return q;
  }

  public String getRoleName() {
    return Deployer.ROLE;
  }

  protected DistributionDatabase getDistributionDatabase() {
    return store;
  }

}
