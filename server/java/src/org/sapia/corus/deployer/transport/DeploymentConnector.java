package org.sapia.corus.deployer.transport;

/**
 * Notified by a <code>DeploymentAcceptor</code> upon deployment by clients.
 * 
 * @see org.sapia.corus.deployer.transport.DeploymentAcceptor#registerConnector(DeploymentConnector) 
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface DeploymentConnector {

  /**
   * @param deployment a <code>Deployment</code>.
   */
  public void connect(Deployment deployment);
}
