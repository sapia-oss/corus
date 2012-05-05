package org.sapia.corus.client;


/**
 * This class models meta-information about an operation performed in the context of a cluster, namely: if
 * the operation is clustered; and if it applies to a selected group of targets in the cluster.
 * 
 * @author Yanick Duchesne
 */
public class ClusterInfo {
  
  private boolean	cluster;
  
  public ClusterInfo(boolean cluster){
  	this.cluster = cluster;
  }
 
  /**
   * @return <code>true</code> if this instance corresponds to a clustered
   * operation.
   */
  public boolean isClustered(){
  	return cluster;
  }
  
}
