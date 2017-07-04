package org.sapia.corus.client.services.deployer.event;

import org.sapia.corus.client.services.deployer.dist.Distribution;

/**
 * Dispatched following an "undeployment".
 * 
 * @author yduchesne
 * 
 */
public class UndeploymentCompletedEvent {

  private Distribution distribution;

  public UndeploymentCompletedEvent(Distribution dist) {
    this.distribution = dist;
  }

  /**
   * @return the {@link Distribution} that was undeployed.
   */
  public Distribution getDistribution() {
    return distribution;
  }

}
