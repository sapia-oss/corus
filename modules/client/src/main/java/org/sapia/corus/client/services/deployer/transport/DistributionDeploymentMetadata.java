package org.sapia.corus.client.services.deployer.transport;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.services.deployer.DeployPreferences;

/**
 * Corresponds to a distribution deployment.
 * 
 * @author yduchesne
 * 
 */
public class DistributionDeploymentMetadata extends DeploymentMetadata {

  private static final long serialVersionUID = 1L;

  public DistributionDeploymentMetadata(String fileName, long contentLen, DeployPreferences prefs, ClusterInfo cluster) {
    super(fileName, contentLen, cluster, DeploymentMetadata.Type.DISTRIBUTION, prefs);
  }

}
