package org.sapia.corus.client.facade;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.services.cluster.ClusterStatus;

/**
 * Specifies methods pertaining to cluster management.
 * 
 * @author yduchesne
 * 
 */
public interface ClusterFacade {

  /**
   * @param cluster
   *          a {@link ClusterInfo} indicating if this method should be
   *          clustered.
   * @return the {@link ClusterStatus} of each node in the cluster on which this
   *         method was called.
   * 
   * @param cluster
   *          a {@link ClusterInfo}.
   */
  public Results<ClusterStatus> getClusterStatus(ClusterInfo cluster);

  /**
   * Forces a resync of the curent node (or all nodes) witht the other nodes in
   * the cluster.
   */
  public void resync();
  
  /**
   * @param newClusterName the name of the new cluster to which to reassign the targeted Corus instance(s).
   */
  public void changeCluster(String newClusterName, ClusterInfo cluster);

}
