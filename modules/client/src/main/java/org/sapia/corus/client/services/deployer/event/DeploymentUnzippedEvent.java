package org.sapia.corus.client.services.deployer.event;

import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.ubik.rmi.interceptor.Event;

/**
 * Dispatched when a distribution has been unzipped and added to Corus, but before post-deploy script execution.
 * 
 * @author yduchesne
 *
 */
public class DeploymentUnzippedEvent implements Event {

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
