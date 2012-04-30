package org.sapia.corus.deployer;

import java.util.Set;

import org.sapia.ubik.net.ServerAddress;


/**
 * This class implements the logic that selects the next server to which a replicated command
 * should be dispatched.
 *
 * @author Yanick Duchesne
 */
public class ReplicationStrategy {
  private Set<ServerAddress> visited;
  private Set<ServerAddress> targets;
  private Set<ServerAddress> siblings;

  /**
   * @param visited the {@link Set} of {@link ServerAddress}es of the hosts
   * that have already been visited.
   * @param targets the {@link Set} of {@link ServerAddress}es corresponding to
   * targeted hosts - if <code>null</code>, then the strategy assumes that all hosts must
   * be visited.
   * @param existing the {@link Set} of {@link ServerAddress}es corresponding to
   * the existing hosts.
   */
  public ReplicationStrategy(
      Set<ServerAddress> visited, 
      Set<ServerAddress> targets, 
      Set<ServerAddress> existing) {
    this.visited    = visited;
    this.targets    = targets;
    this.siblings   = existing;
  }

  /**
   * @return the {@link ServerAddress} of the next sibling host to which replication
   * should be made. <code>null</code> is returned if there is no "next" host to which to
   * replicate - all hosts have been visited. <b>Note</b>: if not null, the returned address
   * is added to the set of visited ones.
   */
  public ServerAddress selectNextSibling() {
    ServerAddress toReturn;

    if (targets == null) {
      siblings.removeAll(visited);

      if (siblings.isEmpty()) {
        return null;
      }

      toReturn = siblings.iterator().next();
    } else {
      siblings.retainAll(targets);
      siblings.removeAll(visited);

      if (siblings.isEmpty()) {
        return null;
      }

      toReturn = siblings.iterator().next();
    }

    visited.add(toReturn);

    return toReturn;
  }
}
