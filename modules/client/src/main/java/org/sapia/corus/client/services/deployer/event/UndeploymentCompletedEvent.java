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
 * Dispatched following an "undeployment".
 * 
 * @author yduchesne
 * 
 */
public class UndeploymentCompletedEvent extends CorusEventSupport {

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
        .message("Distribution %s undeployed successfully", ToStringUtil.toString(distribution))
        .build();
  }
  
  // --------------------------------------------------------------------------
  // Restricted
  
  @Override
  protected Class<?> source() {
    return Deployer.class;
  }
  
  @Override
  protected void toJson(JsonStream stream) {
    stream
      .field("message").value(toEventLog().getMessage())
      .field("distribution");
    distribution.toJson(stream, ContentLevel.SUMMARY);
  }

}
