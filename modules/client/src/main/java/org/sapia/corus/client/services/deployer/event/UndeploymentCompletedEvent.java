package org.sapia.corus.client.services.deployer.event;

import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.ubik.rmi.interceptor.Event;

/**
 * Dispatched following an "undeployment".
 * 
 * @author yduchesne
 * 
 */
public class UndeploymentCompletedEvent implements Event {

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
