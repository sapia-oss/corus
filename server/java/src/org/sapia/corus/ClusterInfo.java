package org.sapia.corus;

import java.util.HashSet;
import java.util.Set;

import org.sapia.ubik.net.ServerAddress;

/**
 * This class models meta-information about an operation performed in the context of a cluster, namely: if
 * the operation is clustered; and if it applies to a selected group of targets in the cluster.
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ClusterInfo {
  
  private Set _targets;
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
  		_targets = new HashSet();
  	}
  	_targets.add(addr);
  	return this;
  }
  
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
  public Set getTargets(){
  	return _targets;
  }
}
