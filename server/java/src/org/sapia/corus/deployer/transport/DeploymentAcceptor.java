package org.sapia.corus.deployer.transport;

/**
 * Accepts incoming connections and dispatches them to a registered
 * <code>DeploymentConnector</code>.
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface DeploymentAcceptor {
  
  /**
   * Initializes this instance.
   */
  public void init() throws Exception;
  
	/**
	 * Starts this instance.
	 */
	public void start() throws Exception;
	
	/**
	 * Stops this instance.
	 */
	public void stop() throws Exception;
	
	/**
	 * @param acceptor a <code>DeploymentConnector</code>.
	 */
	public void registerConnector(DeploymentConnector connector);	
  
}
