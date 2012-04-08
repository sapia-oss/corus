package org.sapia.corus.deployer.transport;

/**
 * Accepts incoming connections and dispatches them to a registered {@link DeploymentConnector}.
 * 
 * @author Yanick Duchesne
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
	 * @param connector a {@link DeploymentConnector}.
	 */
	public void registerConnector(DeploymentConnector connector);	
  
}
