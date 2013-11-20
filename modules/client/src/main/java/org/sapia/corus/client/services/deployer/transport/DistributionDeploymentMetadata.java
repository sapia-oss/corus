package org.sapia.corus.client.services.deployer.transport;

import org.sapia.corus.client.ClusterInfo;

/**
 * Corresponds to a distribution deployment.
 * 
 * @author yduchesne
 * 
 */
public class DistributionDeploymentMetadata extends DeploymentMetadata {

  private static final long serialVersionUID = 1L;

  public DistributionDeploymentMetadata(String fileName, long contentLen, ClusterInfo cluster) {
    super(fileName, contentLen, cluster, DeploymentMetadata.Type.DISTRIBUTION);
  }

}
