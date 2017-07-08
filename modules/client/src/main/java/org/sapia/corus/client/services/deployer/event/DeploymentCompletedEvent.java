package org.sapia.corus.client.services.deployer.event;

import org.sapia.corus.client.services.deployer.dist.Distribution;

/**
 * Dispatched following a deployment.
 * 
 * @author yduchesne
 * 
 */
public class DeploymentCompletedEvent {

  private Distribution distribution;

  public DeploymentCompletedEvent(Distribution dist) {
    this.distribution = dist;
  }

  /**
   * @return the {@link Distribution} that was just deployed.
   */
  public Distribution getDistribution() {
    return distribution;
  }

}
