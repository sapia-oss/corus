package org.sapia.corus.client.services.deployer.transport;

/**
 * Corresponds to a distribution deployment.
 * 
 * @author yduchesne
 *
 */
public class DistributionDeploymentMetadata extends DeploymentMetadata {
  
  private static final long serialVersionUID = 1L;
  
  public DistributionDeploymentMetadata(String fileName, long contentLen, boolean clustered) {
    super(fileName, contentLen, clustered, DeploymentMetadata.Type.DISTRIBUTION);
  }
  
}
