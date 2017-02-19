package org.sapia.corus.client.services.cluster.event;

import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.event.EventLog;
import org.sapia.corus.client.services.event.EventLog.Level;
import org.sapia.corus.client.services.event.Loggable;
import org.sapia.ubik.rmi.interceptor.Event;

/**
 * Event dispatched when a Corus host is added to the current node's cluster view.
 * 
 * @author yduchesne
 *
 */
public class CorusHostAddEvent implements Event, Loggable {
  
  private CorusHost host;
  
  public CorusHostAddEvent(CorusHost host) {
    this.host = host;
  }
  
  /**
   * @return the {@link CorusHost} instance corresponding to the host that appeared.
   */
  public CorusHost getHost() {
    return host;
  }
  
  @Override
  public EventLog getEventLog() {
    return new EventLog(Level.NORMAL, "ClusterManager", "Corus host discovered: " + host.toString());
  }

}
