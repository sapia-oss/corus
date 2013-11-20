package org.sapia.corus.cluster;

import org.sapia.corus.client.services.cluster.CorusHost;

/**
 * An event that is dispatched by a Corus node, at startup, to publish itself to
 * other nodes.
 * 
 * @author Yanick Duchesne
 */
public class CorusPubEvent extends AbstractClusterEvent {

  /**
   * Meant for externalization.
   */
  public CorusPubEvent() {
  }

  public CorusPubEvent(CorusHost hostInfo) {
    super(hostInfo);
  }

}
