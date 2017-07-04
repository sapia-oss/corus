package org.sapia.corus.client.services.deployer.event;

import java.util.Collections;
import java.util.Set;

import org.sapia.ubik.net.ServerAddress;

/**
 * Dispatched when a cascading deployment has been interrupted.
 * 
 * @author yduchesne
 *
 */
public class CascadingDeploymentInterruptedEvent {
  
  private ServerAddress      currentHost;
  private Set<ServerAddress> remainingHosts;
  
  public CascadingDeploymentInterruptedEvent(ServerAddress currentHost, Set<ServerAddress> remainingHosts) {
    this.currentHost    = currentHost;
    this.remainingHosts = remainingHosts;
  }

  /**
   * @return the {@link ServerAddress} corresponding to the Corus node at which the deployment 
   *         was interrupted.
   */
  public ServerAddress getCurrentHost() {
    return currentHost;
  }
  
  /**
   * @return the {@link Set} of {@link ServerAddress} instances corresponding to the Corus nodes
   *         that were targeted by a deployment but to which the deployment failed.
   */
  public Set<ServerAddress> getRemainingHosts() {
    return Collections.unmodifiableSet(remainingHosts);
  }
}
