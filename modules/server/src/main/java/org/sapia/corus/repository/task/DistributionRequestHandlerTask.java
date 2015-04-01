package org.sapia.corus.repository.task;

import java.io.File;
import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.corus.client.services.deployer.transport.DistributionDeploymentMetadata;
import org.sapia.corus.client.services.repository.DistributionDeploymentRequest;
import org.sapia.ubik.util.Func;

/**
 * This task performs the deployment to a provided list of nodes, following a
 * {@link DistributionDeploymentRequest}.
 * 
 * @author yduchesne
 * 
 */
public class DistributionRequestHandlerTask extends ArtifactRequestHandlerTaskSupport {

  /**
   * @param distFile
   *          the {@link File} corresponding to the distribution to deploy.
   * @param targets
   *          the {@link List} of {@link Endpoint}s corresponding to the Corus
   *          nodes to deploy to.
   */
  public DistributionRequestHandlerTask(final File distFile, final List<Endpoint> targets) {
    super(distFile, targets, new Func<DeploymentMetadata, Boolean>() {
      @Override
      public DeploymentMetadata call(Boolean clustered) {
        return new DistributionDeploymentMetadata(
            distFile.getName(), 
            distFile.length(), 
            DeployPreferences.newInstance(),
            new ClusterInfo(clustered).addTargetEndpoints(targets)
        );
      }
    });
  }

}
