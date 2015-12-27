package org.sapia.corus.ext.hook.docker;

import java.io.File;
import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.corus.client.services.deployer.transport.DockerImageDeploymentMetadata;
import org.sapia.corus.repository.task.ArtifactRequestHandlerTaskSupport;
import org.sapia.ubik.util.Func;

/**
 * An instance of this class handles the deployment of a Docker image to given Corus endpoints.
 * 
 * @author yduchesne
 *
 */
public class DockerImageRequestHandlerTask extends ArtifactRequestHandlerTaskSupport {
  
  /**
   * @param imageName 
   *          the name of the Docker image to deploy.
   * @param imageFile
   *          the {@link File} corresponding to the Docker image tarball to deploy.
   * @param targets
   *          the {@link List} of {@link Endpoint}s corresponding to the Corus
   *          nodes to deploy to.
   */
  public DockerImageRequestHandlerTask(final String imageName, final File imageFile, final List<Endpoint> targets) {
    super(imageFile, targets, new Func<DeploymentMetadata, Boolean>() {
      @Override
      public DeploymentMetadata call(Boolean clustered) {
        return new DockerImageDeploymentMetadata(
            imageName,
            imageFile.getName(), 
            imageFile.length(), 
            DeployPreferences.newInstance(),
            new ClusterInfo(clustered).addTargetEndpoints(targets)
        );
      }
    });
  }

}
