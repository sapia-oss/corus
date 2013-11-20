package org.sapia.corus.client.services.deployer.event;

import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.ubik.rmi.interceptor.Event;

/**
 * Dispatched following a deployment.
 * 
 * @author yduchesne
 * 
 */
public class DeploymentEvent implements Event {

  private Distribution distribution;

  public DeploymentEvent(Distribution dist) {
    this.distribution = dist;
  }

  /**
   * @return the {@link Distribution} that was just deployed.
   */
  public Distribution getDistribution() {
    return distribution;
  }

}
