package org.sapia.corus.client.services.deployer.event;

import org.sapia.corus.client.services.deployer.dist.Distribution;

/**
 * Dispatched prior to undeploying.
 * 
 * @author yduchesne
 * 
 */
public class UndeploymentStartingEvent {

  private Distribution distribution;

  public UndeploymentStartingEvent(Distribution dist) {
    this.distribution = dist;
  }

  /**
   * @return the {@link Distribution} that was undeployed.
   */
  public Distribution getDistribution() {
    return distribution;
  }

}
