package org.sapia.corus.client.services.deployer.event;

import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;


/**
 * Dispatched when streaming of a deployed artifact has completed with a failure.
 * 
 * @author yduchesne
 *
 */
public class DeploymentStreamingFailedEvent {
  
  private DeploymentMetadata deploymentMetadata;
  
  public DeploymentStreamingFailedEvent(DeploymentMetadata deploymentMetadata) {
    this.deploymentMetadata = deploymentMetadata;
  }
  
  /**
   * @return the {@link DeploymentMetadata} corresponding to the artifact being deployed.
   */
  public DeploymentMetadata getDeploymentMetadata() {
    return deploymentMetadata;
  }
}
