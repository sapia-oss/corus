package org.sapia.corus.client.services.deployer.transport;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.services.deployer.DeployPreferences;

/**
 * Corresponds to a Docker image deployment.
 * 
 * @author yduchesne
 * 
 */
public class DockerImageDeploymentMetadata extends DeploymentMetadata {

  private static final long serialVersionUID = 1L;
  
  private String imageName;

  public DockerImageDeploymentMetadata(String imageName, String fileName, long contentLen, DeployPreferences prefs, ClusterInfo cluster) {
    super(fileName, contentLen, cluster, DeploymentMetadata.Type.DOCKER_IMAGE, prefs);
    this.imageName = imageName;
  }
  
  /**
   * @return the name of the Docker image to which this instance corresponds.
   */
  public String getImageName() {
    return imageName;
  }
}
