package org.sapia.corus.client.services.cluster.event;

import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.event.EventLog;
import org.sapia.corus.client.services.event.EventLog.Level;
import org.sapia.corus.client.services.event.Loggable;
import org.sapia.ubik.rmi.interceptor.Event;

/**
 * Event dispatched when a Corus host is removed from the current node's cluster view.
 * 
 * @author yduchesne
 *
 */
public class CorusHostRemovedEvent implements Event, Loggable {
  
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
  public EventLog getEventLog() {
    return new EventLog(Level.NORMAL, "ClusterManager", "Corus host removed (down or terminated): " + host.toString());
  }

}
