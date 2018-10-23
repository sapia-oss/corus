package org.sapia.corus.client.services.deployer.event;

import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.event.CorusEventSupport;
import org.sapia.corus.client.services.event.EventLevel;
import org.sapia.corus.client.services.event.EventLog;

/**
 * Dispatched in the context of a rollback having completed successfully.
 * 
 * @author yduchesne
 * 
 */
public class RollbackCompletedEvent extends CorusEventSupport {

  private Distribution distribution;
  private RollbackType         type;

  public RollbackCompletedEvent(Distribution dist, RollbackType type) {
    this.distribution = dist;
    this.type         = type;
  }

  /**
   * @return the {@link Distribution} that was just deployed.
   */
  public Distribution getDistribution() {
    return distribution;
  }
  
  /**
   * @return this instance's {@link RollbackType}.
   */
  public RollbackType getRollbackType() {
    return type;
  }
  
  @Override
  public EventLevel getLevel() {
    return EventLevel.INFO;
  }
  
  @Override
  public EventLog toEventLog() {
    return EventLog.builder()
        .source(source())
        .type(getClass())
        .level(getLevel())
        .message("Rollback successful for distribution %s", ToStringUtil.toString(distribution))
        .build();
  }
  
  // --------------------------------------------------------------------------
  // Restricted
  
  @Override
  protected Class<?> source() {
    return Deployer.class;
  }
  
  protected void toJson(JsonStream stream) {
    stream
      .field("message").value(toEventLog().getMessage())
      .field("type").value(type.name())
      .field("distribution");
    distribution.toJson(stream, ContentLevel.SUMMARY);
  }
}
