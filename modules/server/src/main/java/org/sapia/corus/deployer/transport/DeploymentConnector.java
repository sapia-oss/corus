package org.sapia.corus.deployer.transport;

/**
 * Notified by a <code>DeploymentAcceptor</code> upon deployment by clients.
 * 
 * @see org.sapia.corus.deployer.transport.DeploymentAcceptor#registerConnector(DeploymentConnector) 
 * 
 * @author Yanick Duchesne
 */
public interface DeploymentConnector {

  /**
   * @param deployment a <code>Deployment</code>.
   */
  public void connect(Deployment deployment);
}
