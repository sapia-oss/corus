package org.sapia.corus.cluster;

import org.sapia.corus.client.services.cluster.ServerHost;
import org.sapia.ubik.net.ServerAddress;

/**
 * An event that is sent by a Corus node to a node that just published itself (therefore, in
 * response to a {@link CorusPubEvent}).
 * 
 * @author Yanick Duchesne
 */
public class CorusDiscoEvent extends AbstractClusterEvent {
  
	/**
	 * Meant for externalization
	 */
	public CorusDiscoEvent() {
  }
	
  public CorusDiscoEvent(ServerAddress origin, ServerHost hostInfo) {
  	super(origin, hostInfo);
  }
  
}
