package org.sapia.corus;

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
  
  private Set<ServerAddress> _targets;
  //private Set<String> _tags;
  private boolean _cluster;
  
  public ClusterInfo(boolean cluster){
  	_cluster = cluster;
  }
  
  /**
   * Adds the given deployment target.
   *   
   * @param addr a <code>ServerAddress</code> corresponding to the target
   * server.
   * @return this instance (allowing for chained invocation).
   */
  public ClusterInfo addTarget(ServerAddress addr){
  	_cluster = true;
  	if(_targets == null){
  		_targets = new HashSet<ServerAddress>();
  	}
  	_targets.add(addr);
  	return this;
  }
  
  /**
   * Adds the given tag.
   * @param tag a tag that target server must match.
   * @return
   */
  /*public ClusterInfo addTag(String tag){
    _cluster = true;
    if(_tags == null){
      _tags = new HashSet<String>();
    }
    _tags.add(tag);
    return this;
  }*/
    
  /**
   * @return <code>true</code> if this instance corresponds to a clustered
   * operation.
   */
  public boolean isClustered(){
  	return _cluster;
  }
  
  /**
   * Returns the targets to which this instance's operation corresponds. If the
   * returned set is <code>null</code> and this instance's <code>isClustered()</code>
   * method returns <code>true</code>, then this instance indicates that its
   * corresponding operation should be performed accross all instances in the cluster.
   * <p>
   * If the returned set is not <code>null</code>, then only the corresponding servers
   * should be targeted by the operation.
   * 
   * @return the <code>Set</code> of <code>ServerAddress</code>es corresponding
   * to this instance's targets, or <code>null</code> if no targets were
   * specified.
   */
  public Set<ServerAddress> getTargets(){
  	return _targets;
  }
  
  /**
   * Returns the tags to which this instance's operation corresponds. If the
   * returned set is <code>null</code> and this instance's <code>isClustered()</code>
   * method returns <code>true</code>, then this instance indicates that its
   * corresponding operation should be performed accross all instances in the cluster.
   * <p>
   * If the returned set is not <code>null</code>, then only servers whose tags
   * match this instance's should be targeted by the operation.
   * 
   * @return the <code>Set</code> of tags corresponding
   * to this instance's targets, or <code>null</code> if no tags were
   * specified.
   */
  /*public Set<String> getTags() {
    return _tags;
  }*/
}
