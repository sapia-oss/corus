package org.sapia.corus.client.services.deployer.event;

import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.event.CorusEventSupport;
import org.sapia.corus.client.services.event.EventLevel;
import org.sapia.corus.client.services.event.EventLog;

/**
 * Dispatched if an error occurs at deployment time. Precedes the corresponding {@link RollbackCompletedEvent}, 
 * if a rollback is automatically triggered upon the error occurring.
 * 
 * @author yduchesne
 * 
 */
public class DeploymentFailedEvent extends CorusEventSupport {

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
  
  @Override
  public EventLevel getLevel() {
    return EventLevel.ERROR;
  }
  
  @Override
  public EventLog toEventLog() {
    EventLog.Builder builder = EventLog.builder()
        .source(source())
        .type(getClass())
        .level(getLevel());
    distribution
      .ifSet(dist -> {
        builder
          .message("Deployment failed for distribution %s - check the logs for details", ToStringUtil.toString(dist));
      })
      .ifNull(() -> {
        builder
          .message("Error deploying distribution - distribution details ")
          .message("not available since it could not be unpacked. Check the logs for more information");
      });
    
    return builder.build();
  }

  // --------------------------------------------------------------------------
  // Restricted
  
  @Override
  protected Class<?> source() {
    return Deployer.class;
  }
  
  @Override
  protected void toJson(JsonStream stream) {
    stream.field("message").value(toEventLog().getMessage());
    distribution
      .ifSet(dist -> {
        stream.field("distribution");
        dist.toJson(stream, ContentLevel.SUMMARY); 
      });
  }
  
}
