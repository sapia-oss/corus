package org.sapia.corus.cluster;

import org.sapia.corus.Module;
import org.sapia.ubik.mcast.EventChannel;

import java.util.Set;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface ClusterManager extends java.rmi.Remote, Module {
  public static String ROLE = ClusterManager.class.getName();

  /**
   * Returns addresses of the other coruss in the cluster.
   *
   * @return a <code>Set</code> of <code>ServerAddress</code>
   * instances.
   * @see org.sapia.ubik.net.ServerAddress
   */
  public Set getHostAddresses();

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
