package org.sapia.corus.deployer.transport;

/**
 * Notified by a {@link DeploymentAcceptor} upon deployment by clients.
 * 
 * @see org.sapia.corus.deployer.transport.DeploymentAcceptor#registerConnector(DeploymentConnector)
 * 
 * @author Yanick Duchesne
 */
public interface DeploymentConnector {

  /**
   * @param deployment
   *          a {@link Deployment}.
   */
  public void connect(Deployment deployment);
}
