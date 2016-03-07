package org.sapia.corus.client;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.server.transport.http.HttpAddress;
import org.sapia.ubik.util.Strings;

/**
 * This class models meta-information about an operation performed in the
 * context of a cluster, namely: if the operation is clustered; and if it
 * applies to a selected group of targets in the cluster.
 * 
 * @author Yanick Duchesne
 */
public class ClusterInfo implements Serializable {

  private static final int HOST_INDEX = 0;
  private static final int PORT_INDEX = 1;
  private static final long serialVersionUID = 1L;

  private boolean cluster;
  private Set<ServerAddress> targets  = new HashSet<>();
  private Set<ServerAddress> excluded = new HashSet<>();

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
   * @return this instance.
   */
  public ClusterInfo addTarget(ServerAddress target) {
    targets.add(target);
    return this;
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

  /**
   * @param endpoints a {@link Collection} of {@link Endpoint}s whose addresses
   * correspond to targeted Corus servers.
   * 
   * @return this instance.
   */
  public ClusterInfo addTargetEndpoints(Collection<Endpoint> endpoints) {
    for (Endpoint ep : endpoints) {
      this.targets.add(ep.getServerAddress());
    }
    return this;
  }
  
  /**
   * @param hosts a Collection of {@link CorusHost}s whose addresses
   * correspond to targeted Corus servers.
   * 
   * @return this instance.
   */
  public ClusterInfo addTargetHosts(Collection<CorusHost> hosts) {
    for (CorusHost h : hosts) {
      this.targets.add(h.getEndpoint().getServerAddress());
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
   * @param target the {@link ServerAddress} corresponding to the target to test for.
   * @return <code>true</code> if a single host is targeted, which corresponds to the given target.
   */
  public boolean isTargetingSingleHost(ServerAddress target) {
    return targets.size() == 1 && targets.contains(target);
  }
  
  /**
   * @param target the {@link ServerAddress} corresponding to the target to test for.
   * @return <code>true</code> the given target is included in this instance's target set, or
   * if all hosts are targeted.
   */
  public boolean isTargetingHost(ServerAddress target) {
    return targets.contains(target) || targets.isEmpty();
  }

  /**
   * @return a copy of this instance's target {@link ServerAddress}es.
   */
  public Set<ServerAddress> getTargets() {
    return new HashSet<ServerAddress>(targets);
  }
  
  /**
   * @return a copy of this instance excluded {@link ServerAddress}es.
   */
  public Set<ServerAddress> getExcluded() {
    return new HashSet<ServerAddress>(excluded);
  }
  
  /**
   * Adds the given {@link ServerAddress} to this instance's excluded set.
   * 
   * @param target the {@link ServerAddress} corresponding to the target to exclude.
   * @see #getTargets()
   * @see #getExcluded()
   */
  public ClusterInfo addExcluded(ServerAddress target) {
    excluded.add(target);
    return this;
  }
  
  /**
   * @return a string representation that can be parsed.
   */
  public String toLiteralForm() {
    StringBuilder s = new StringBuilder();
    for (ServerAddress addr : targets) {
      TCPAddress tcpAddr = (TCPAddress) addr;
      if (s.length() > 0) {
        s.append(",");
      }
      s.append(tcpAddr.getHost()).append(":").append(tcpAddr.getPort());
    }
    return s.toString();
  }
  
  /**
   * This method parses the given literal form into a new instance of this class. The form must
   * respect the following syntax:
   * <pre>
   * host_0:port_0[host_1:port_1[...,host_N:port_N]]
   * </pre>
   * 
   * @param literalForm a literal form.
   * @return a new {@link ClusterInfo} instance, based on the given literal form.
   */
  public static ClusterInfo fromLiteralForm(String literalForm) {
    String[] addresses = StringUtils.split(literalForm, ',');
    ClusterInfo cluster = new ClusterInfo(true);
    for (String addressLiteral : addresses) {
     String[] parts = StringUtils.split(addressLiteral, ':');
     if (parts.length != 2) {
       throw new IllegalArgumentException("Expected host:port, got: " + addressLiteral);
     }
     cluster.addTarget(HttpAddress.newDefaultInstance(parts[HOST_INDEX], Integer.parseInt(parts[PORT_INDEX])));
    }
    return cluster;
  }

  /**
   * @return returns a {@link ClusterInfo} whose <code>clustered</code> flag is <code>false</code>.
   */
  public static ClusterInfo clustered() {
    return new ClusterInfo(true);
  }
  
  /**
   * @return returns a {@link ClusterInfo} whose <code>clustered</code> flag is <code>false</code>.
   */
  public static ClusterInfo notClustered() {
    return new ClusterInfo(false);
  }
  
  @Override
  public String toString() {
    return Strings.toStringFor(this, "clustered", cluster, "targets", targets, "excluded", excluded);
  }

}
