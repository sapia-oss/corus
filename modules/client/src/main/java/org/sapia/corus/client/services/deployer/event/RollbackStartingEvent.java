package org.sapia.corus.client.services.deployer.event;

import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.ubik.rmi.interceptor.Event;

/**
 * Dispatched prior to the packaged rollback script being executed.
 * 
 * @author yduchesne
 *
 */
public class RollbackStartingEvent implements Event {

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
