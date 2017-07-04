package org.sapia.corus.client.services.deployer.event;

import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.services.deployer.dist.Distribution;

/**
 * Dispatched if an error occurs at deployment time. Precedes the corresponding {@link RollbackCompletedEvent}, 
 * if a rollback is automatically triggered upon the error occurring.
 * 
 * @author yduchesne
 * 
 */
public class DeploymentFailedEvent {

  private OptionalValue<Distribution> distribution;

  public DeploymentFailedEvent(Distribution dist) {
    this(OptionalValue.of(dist));
  }
  
  public DeploymentFailedEvent() {
    distribution = OptionalValue.none();
  }
  
  public DeploymentFailedEvent(OptionalValue<Distribution> dist) {
    this.distribution = dist;
  }

  /**
   * @return the {@link Distribution} whose deployment fail, or an {@link OptionalValue} with
   * an unset reference if the distribution could not even be unpacked.
   */
  public OptionalValue<Distribution> getDistribution() {
    return distribution;
  }

}
