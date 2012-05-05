package org.sapia.corus.client.services.cluster;

import java.util.Set;

import org.sapia.ubik.net.ServerAddress;

/**
 * This class implements the logic that selects the next server to which a replicated command
 * should be dispatched.
 *
 * @author Yanick Duchesne
 */
public final class ClusteringHelper {
  
  private ClusteringHelper() {
  }

  /**
   * @param visited the {@link Set} of {@link ServerAddress}es of the hosts
   * that have already been visited.
   * @param siblings the {@link Set} of {@link ServerAddress}es corresponding to
   * the hosts that are siblings of the currently visited one.
   * @return the {@link ServerAddress} of the next target host to which replication
   * should be made. <code>null</code> is returned if there is no "next" host to which to
   * replicate - all hosts have been visited. <b>Note</b>: if not null, the returned address
   * is added to the set of visited ones.
   */
  public static ServerAddress selectNextTarget(      
      Set<ServerAddress> visited, 
      Set<ServerAddress> siblings) {
    
    ServerAddress toReturn;
    siblings.removeAll(visited);

    if (siblings.isEmpty()) {
      return null;
    }

    toReturn = siblings.iterator().next();
    visited.add(toReturn);
    return toReturn;
  }
}
