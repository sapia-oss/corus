package org.sapia.corus.cloud.platform.domain;

import java.util.Objects;

import org.sapia.corus.cloud.topology.Cluster;
import org.sapia.corus.cloud.topology.Env;
import org.sapia.corus.cloud.topology.Region;
import org.sapia.corus.cloud.topology.Zone;

/**
 * Corresponds to a Corus instance in the cloud, that is: in given region/zone/environment and cluster.
 * 
 * @author yduchesne
 *
 */
public class CorusInstance {
  
  private Region  region;
  private Zone    zone;
  private Env     env;
  private Cluster cluster;
  private CorusAddress address;

  /**
   * @param region a {@link Region}.
   * @param zone a {@link Zone}.
   * @param env an {@link Env} instance corresponding to an environment.
   * @param cluster a {@link Cluster}.
   * @param address a {@link CorusAddress}, corresponding to a Corus host in the given cluster.
   */
  public CorusInstance(Region region, Zone zone, Env env, Cluster cluster, CorusAddress address) {
    this.region    = region;
    this.zone      = zone;
    this.env       = env;
    this.cluster   = cluster;
    this.address   = address;
  }
  
  /**
   * @return the {@link CorusAddress}.
   */
  public CorusAddress getAddress() {
    return address;
  }
  
  /**
   * @return the Corus cluster.
   */
  public Cluster getCluster() {
    return cluster;
  }

  /**
   * @return the cloud environment of the Corus instance.
   */
  public Env getEnv() {
    return env;
  }
  
  /**
   * @return the cloud availability {@link Zone} of the Corus instance.
   */
  public Zone getZone() {
    return zone;
  }
  
  /**
   * @return the cloud {@link Region} of the Corus instance.
   */
  public Region getRegion() {
    return region;
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public String toString() {
    return new StringBuilder()
        .append("[region=").append(region.getName())
        .append(", zone=").append(zone.getName())
        .append(", env=").append(env.getName())
        .append(", cluster=").append(cluster.getName())
        .append(", address=").append(address).append("]")
        .toString();
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(region, zone, env, cluster, address);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CorusInstance) {
      CorusInstance other = (CorusInstance) obj;
      return region.equals(other.getRegion())
          && zone.equals(other.getZone())
          && env.equals(other.getEnv())
          && cluster.equals(other.getCluster())
          && address.equals(other.getEnv());
    }
    return false;
  }
}
