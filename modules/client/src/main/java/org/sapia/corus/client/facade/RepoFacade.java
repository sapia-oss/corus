package org.sapia.corus.client.facade;

import org.sapia.corus.client.ClusterInfo;

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

}
