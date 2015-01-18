package org.sapia.corus.client.facade;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;

/**
 * Provides Repository-related methods.
 * 
 * @author yduchesne
 * 
 */
public interface RepoFacade {

  /**
   * Triggers a pull from Corus repo server nodes.
   * 
   * @param cluster
   *          a {@link ClusterInfo}.
   */
  public void pull(ClusterInfo cluster);

  /**
   * Triggers a push to Corus repo client nodes.
   * 
   * @param cluster
   *          a {@link ClusterInfo}.
   */
  public void push(ClusterInfo cluster);
  
  /**
   * Changes the {@link RepoRole} of given Corus nodes.
   * 
   * @param newRole the {@link RepoRole} to change to.
   * @param cluster 
   *          a {@link ClusterInfo}.
   */
  public void changeRole(RepoRole newRole, ClusterInfo cluster);

}
