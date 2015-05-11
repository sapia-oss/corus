package org.sapia.corus.repository.task;

import java.io.File;
import java.util.List;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.ShellScript;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.corus.client.services.deployer.transport.ShellScriptDeploymentMetadata;
import org.sapia.corus.client.services.repository.ShellScriptDeploymentRequest;
import org.sapia.ubik.util.Func;

/**
 * This task performs the deployment to a provided list of nodes, following a
 * {@link ShellScriptDeploymentRequest}.
 * 
 * @author yduchesne
 * 
 */
public class ShellScriptRequestHandlerTask extends ArtifactRequestHandlerTaskSupport {

  /**
   * @param scriptFile
   *          the {@link File} consisting of the shell script to deploy.
   * @param script
   *          the {@link ShellScript} holding the script's information.
   * @param targets
   *          the {@link List} of {@link Endpoint}s corresponding to the Corus
   *          nodes to deploy to.
   */
  public ShellScriptRequestHandlerTask(final File scriptFile, final ShellScript script, final List<Endpoint> targets) {
    super(scriptFile, targets, new Func<DeploymentMetadata, Boolean>() {
      @Override
      public DeploymentMetadata call(Boolean clustered) {
        return new ShellScriptDeploymentMetadata(script.getFileName(), scriptFile.length(), script.getAlias(), script.getDescription(),
            new ClusterInfo(clustered).addTargetEndpoints(targets));
      }
    });
  }

}
