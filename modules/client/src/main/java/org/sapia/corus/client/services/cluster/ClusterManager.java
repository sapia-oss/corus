package org.sapia.corus.client.services.cluster;

import java.util.Set;

import org.sapia.corus.client.Module;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.net.ServerAddress;


/**
 * This module provides clustering services (it is in charge of discovering other Corus
 * instances in the cluster, and reporting its own Corus instance to the cluster).
 * 
 * @author Yanick Duchesne
 */
public interface ClusterManager extends java.rmi.Remote, Module {
  
  public static String ROLE = ClusterManager.class.getName();

  /**
   * Returns addresses of the other Corus servers in the cluster/domain.
   *
   * @return a <code>Set</code> of <code>ServerAddress</code>
   * instances.
   * @see org.sapia.ubik.net.ServerAddress
   */
  public Set<ServerAddress> getHostAddresses();

  /**
   * Returns information of the other Corus servers in the cluster/domain.
   *
   * @return a <code>Set</code> of {@link ServerHost} instances.
   * @see org.sapia.ubik.net.ServerAddress
   */
  public Set<ServerHost> getHosts();

  /**
   * Returns the event channel used to dispatch events to
   * the other coruss in the cluster.
   * <p>
   * IMPORTANT: this method cannot be called remotely.
   *
   * @return an <code>EventChannel</code> instance.
   */
  public EventChannel getEventChannel();
}
