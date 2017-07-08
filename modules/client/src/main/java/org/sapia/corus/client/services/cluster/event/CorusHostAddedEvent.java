package org.sapia.corus.client.services.cluster.event;

import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.event.EventLog;
import org.sapia.corus.client.services.event.EventLevel;
import org.sapia.corus.client.services.event.EventLogCapable;

/**
 * Event dispatched when a Corus host is added to the current node's cluster view.
 * 
 * @author yduchesne
 *
 */
public class CorusHostAddedEvent implements EventLogCapable {
  
  private CorusHost host;
  
  public CorusHostAddedEvent(CorusHost host) {
    this.host = host;
  }
  
  /**
   * @return the {@link CorusHost} instance corresponding to the host that appeared.
   */
  public CorusHost getHost() {
    return host;
  }
  
  @Override
  public EventLog toEventLog() {
    return new EventLog(EventLevel.INFO, "ClusterManager", "Corus host discovered: " + host.toString());
  }

}
