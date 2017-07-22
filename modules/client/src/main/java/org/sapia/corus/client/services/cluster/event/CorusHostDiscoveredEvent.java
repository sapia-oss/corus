package org.sapia.corus.client.services.cluster.event;

import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.event.CorusEventSupport;
import org.sapia.corus.client.services.event.EventLevel;
import org.sapia.corus.client.services.event.EventLog;

/**
 * Event dispatched when a Corus host is added to the current node's cluster view.
 * 
 * @author yduchesne
 *
 */
public class CorusHostDiscoveredEvent extends CorusEventSupport {
  
  private CorusHost host;
  
  public CorusHostDiscoveredEvent(CorusHost host) {
    this.host = host;
  }
  
  /**
   * @return the {@link CorusHost} instance corresponding to the host that appeared.
   */
  public CorusHost getHost() {
    return host;
  }
  
  @Override
  public EventLevel getLevel() {
    return EventLevel.TRACE;
  }
  
  @Override
  public EventLog toEventLog() {
    return EventLog.builder()
        .source(source())
        .type(CorusHostDiscoveredEvent.class)
        .level(getLevel())
        .message("Corus host discovered: %s", host)
        .build();
  }
  
  @Override
  protected Class<?> source() {
    return ClusterManager.class;
  }
  
  @Override
  protected void toJson(JsonStream stream) {
    stream
      .field("message").value(toEventLog().getMessage())
      .field("discoveredHost");
    host.toJson(stream, ContentLevel.SUMMARY);
  }

}
