package org.sapia.corus.client.services.cluster.event;

import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.event.EventLog;
import org.sapia.corus.client.services.event.EventLevel;
import org.sapia.corus.client.services.event.EventLogCapable;

/**
 * Event dispatched when a Corus host is removed from the current node's cluster view.
 * 
 * @author yduchesne
 *
 */
public class CorusHostRemovedEvent implements Event, EventLogCapable {
  
  private CorusHost host;
  
  public CorusHostRemovedEvent(CorusHost host) {
    this.host = host;
  }
  
  /**
   * @return the {@link CorusHost} instance corresponding to the host that disappeared.
   */
  public CorusHost getHost() {
    return host;
  }
  
  @Override
  public EventLog toEventLog() {
    return new EventLog(EventLevel.INFO, "ClusterManager", "Corus host removed (down or terminated): " + host.toString());
  }

}
