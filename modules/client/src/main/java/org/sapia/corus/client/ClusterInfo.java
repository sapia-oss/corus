package org.sapia.corus.client;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.ubik.net.ServerAddress;

/**
 * This class models meta-information about an operation performed in the
 * context of a cluster, namely: if the operation is clustered; and if it
 * applies to a selected group of targets in the cluster.
 * 
 * @author Yanick Duchesne
 */
public class ClusterInfo implements Serializable {

  private static final long serialVersionUID = 1L;

  private boolean cluster;
  private Set<ServerAddress> targets = new HashSet<ServerAddress>();

  public ClusterInfo(boolean cluster) {
    this.cluster = cluster;
  }

  /**
   * @return <code>true</code> if this instance corresponds to a clustered
   *         operation.
   */
  public boolean isClustered() {
    return cluster;
  }

  /**
   * @param target
   *          the {@link ServerAddress} of a target.
   */
  public void addTarget(ServerAddress target) {
    targets.add(target);
  }

  /**
   * @param targets
   *          a {@link Set} of {@link ServerAddress}es corresponding to target
   *          Corus servers.
   * @return this instance.
   */
  public ClusterInfo addTargets(Set<ServerAddress> targets) {
    this.targets.addAll(targets);
    return this;
  }

  /*
   * @param endpoints a {@link Collection} of {@link Endpoint}s whose addresses
   * correspond to target Corus servers.
   * 
   * @return this instance.
   */
  public ClusterInfo addTargets(Collection<Endpoint> endpoints) {
    for (Endpoint ep : endpoints) {
      this.targets.add(ep.getServerAddress());
    }
    return this;
  }

  /**
   * @return <code>true</code> if this instance
   */
  public boolean isTargetingAllHosts() {
    return targets.isEmpty();
  }

  /**
   * @return a copy of this instance's target {@link ServerAddress}es.
   */
  public Set<ServerAddress> getTargets() {
    return new HashSet<ServerAddress>(targets);
  }
}
