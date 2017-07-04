package org.sapia.corus.client.services.deployer.event;

import org.sapia.corus.client.services.deployer.dist.Distribution;

/**
 * Dispatched prior to the packaged rollback script being executed.
 * 
 * @author yduchesne
 *
 */
public class RollbackStartingEvent {

  private Distribution distribution;

  public RollbackStartingEvent(Distribution dist) {
    this.distribution = dist;
  }

  /**
   * @return the {@link Distribution} being rolled back.
   */
  public Distribution getDistribution() {
    return distribution;
  }
}
