package org.sapia.corus.client;

import java.util.HashSet;
import java.util.Set;

import org.sapia.ubik.net.ServerAddress;

/**
 * This class models meta-information about an operation performed in the context of a cluster, namely: if
 * the operation is clustered; and if it applies to a selected group of targets in the cluster.
 * 
 * @author Yanick Duchesne
 */
public class ClusterInfo {
  
  private Set<ServerAddress> targets;
  private boolean 					 cluster;
  
  public ClusterInfo(boolean cluster){
  	this.cluster = cluster;
  }
  
  /**
   * Adds the given deployment target.
   *   
   * @param addr a {@link ServerAddress} corresponding to the targeted
   * Corus server.
   * @return this instance (allowing for chained invocation).
   */
  public ClusterInfo addTarget(ServerAddress addr){
  	cluster = true;
  	if(targets == null){
  		targets = new HashSet<ServerAddress>();
  	}
  	targets.add(addr);
  	return this;
  }
 
  /**
   * @return <code>true</code> if this instance corresponds to a clustered
   * operation.
   */
  public boolean isClustered(){
  	return cluster;
  }
  
  /**
   * Returns the targets to which this instance's operation corresponds. If the
   * returned set is <code>null</code> and this instance's {@link #isClustered()}
   * method returns <code>true</code>, then this instance indicates that its
   * corresponding operation should be performed accross all instances in the cluster.
   * <p>
   * If the returned set is not <code>null</code>, then only the corresponding servers
   * should be targeted by the operation.
   * 
   * @return the {@link Set} of {@link ServerAddress}es corresponding
   * to this instance's targets, or <code>null</code> if no targets were
   * specified.
   */
  public Set<ServerAddress> getTargets(){
  	return targets;
  }
  
}
