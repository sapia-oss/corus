package org.sapia.corus.cluster;

import org.sapia.corus.client.services.cluster.CorusHost;

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
	
  public CorusDiscoEvent(CorusHost origin) {
  	super(origin);
  }
  
}
