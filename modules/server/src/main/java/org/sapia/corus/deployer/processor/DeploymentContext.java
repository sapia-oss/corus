package org.sapia.corus.deployer.processor;

import org.sapia.corus.client.services.deployer.dist.Distribution;

/**
 * Holds post-deployment data.
 * 
 * @author yduchesne
 *
 */
public class DeploymentContext {

  private Distribution distribution;
  
  public DeploymentContext(Distribution distribution) {
    this.distribution = distribution;
  }
  
  /**
   * @return the {@link Distribution} that was just deployed.
   */
  public Distribution getDistribution() {
    return distribution;
  }
}
