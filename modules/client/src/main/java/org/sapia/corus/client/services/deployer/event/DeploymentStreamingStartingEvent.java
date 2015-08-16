package org.sapia.corus.client.services.deployer.event;

import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.ubik.rmi.interceptor.Event;

/**
 * Dispatched when streaming of a deployed artifact starts.
 * 
 * @author yduchesne
 *
 */
public class DeploymentStreamingStartingEvent implements Event {
  
  private DeploymentMetadata deploymentMetadata;
  
  public DeploymentStreamingStartingEvent(DeploymentMetadata deploymentMetadata) {
    this.deploymentMetadata = deploymentMetadata;
  }
  
  /**
   * @return the {@link DeploymentMetadata} corresponding to the artifact being deployed.
   */
  public DeploymentMetadata getDeploymentMetadata() {
    return deploymentMetadata;
  }
}
