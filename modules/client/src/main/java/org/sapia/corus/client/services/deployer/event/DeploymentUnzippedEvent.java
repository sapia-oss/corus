package org.sapia.corus.client.services.deployer.event;

import org.sapia.corus.client.services.deployer.dist.Distribution;

/**
 * Dispatched when a distribution has been unzipped and added to Corus, but before post-deploy script execution.
 * 
 * @author yduchesne
 *
 */
public class DeploymentUnzippedEvent {

  private Distribution distribution;

  public DeploymentUnzippedEvent(Distribution dist) {
    this.distribution = dist;
  }

  /**
   * @return the {@link Distribution} that was just deployed.
   */
  public Distribution getDistribution() {
    return distribution;
  }
}
