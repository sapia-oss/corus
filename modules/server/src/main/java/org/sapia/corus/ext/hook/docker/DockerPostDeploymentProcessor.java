package org.sapia.corus.ext.hook.docker;

import java.util.HashSet;
import java.util.Set;

import org.sapia.corus.client.common.LogCallback;
import org.sapia.corus.client.common.PairTuple;
import org.sapia.corus.client.common.ToStringUtils;
import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.configurator.Tag;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.deployer.dist.Starter;
import org.sapia.corus.client.services.deployer.dist.docker.DockerStarter;
import org.sapia.corus.deployer.processor.DeploymentContext;
import org.sapia.corus.deployer.processor.DeploymentPostProcessor;
import org.sapia.corus.deployer.processor.UndeploymentPostProcessor;
import org.sapia.corus.docker.DockerFacade;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;

/**
 * This class holds logic for synchronizing deployment/undeployment with Docker.
 *
 * @author yduchesne
 *
 */
public class DockerPostDeploymentProcessor implements DeploymentPostProcessor, UndeploymentPostProcessor {

  private final Configurator configurator;

  private final DockerFacade dockerFacade;

  /**
   * Creates a new {@link DockerPostDeploymentProcessor} instance with the arguments passed in.
   *
   * @param configurator The corus configurator.
   * @param dockerFacade The docker facade.
   */
  public DockerPostDeploymentProcessor(Configurator configurator, DockerFacade dockerFacade) {
    this.configurator = configurator;
    this.dockerFacade = dockerFacade;
  }

  // --------------------------------------------------------------------------
  // DeploymentPostProcessor interface

  @Override
  public void onPostDeploy(final DeploymentContext context, final LogCallback callback) {
    onPostProcess(context, callback, new Func<Void, PairTuple<String,DockerFacade>>() {
      @Override
      public Void call(PairTuple<String, DockerFacade> data) {
        callback.debug(String.format(
            "Removing Docker image %s in the context of distribution deployment for: %s",
            data.getLeft(), ToStringUtils.toString(context.getDistribution())
        ));

        data.getRight().pullImage(data.getLeft(), callback);
        return null;
      }
    });
  }

  // --------------------------------------------------------------------------
  // UndeploymentPostProcessor interface

  @Override
  public void onPostUndeploy(final DeploymentContext context, final LogCallback callback)
      throws Exception {
    onPostProcess(context, callback, new Func<Void, PairTuple<String,DockerFacade>>() {
      @Override
      public Void call(PairTuple<String, DockerFacade> data) {
        callback.debug(String.format(
            "Removing Docker image %s in the context distribution undeployment for: %s",
            data.getLeft(), ToStringUtils.toString(context.getDistribution())
        ));

        data.getRight().removeImage(data.getLeft(), callback);
        return null;
      }
    });
  }

  // --------------------------------------------------------------------------
  // Restricted

  private void onPostProcess(DeploymentContext context, LogCallback callback, Func<Void, PairTuple<String, DockerFacade>> processingFunction) {

    Set<String> processedImages = new HashSet<String>();
    Set<String> corusTags = Collects.convertAsSet(configurator.getTags(), new Func<String, Tag>() {
      @Override
      public String call(Tag t) {
        return t.getValue();
      }
    });

    for (ProcessConfig pc : context.getDistribution().getProcesses()) {
      Set<String> processTags = new HashSet<String>(context.getDistribution().getTagSet());
      processTags.addAll(pc.getTagSet());
      if (corusTags.containsAll(processTags)) {
        if (!processTags.isEmpty()) {
          callback.info(
              String.format("Process tags (%s) matched Corus tags, will proceed to deployment of Docker image",
              ToStringUtils.joinToString(processTags))
          );
        }

        for (Starter st : pc.getStarters()) {
          if (st instanceof DockerStarter) {
            DockerStarter dst = (DockerStarter) st;
            String imageName = null;
            if (dst.getImage().isSet()) {
              imageName = dst.getImage().get();
            } else {
              imageName = context.getDistribution().getName() + ":" + context.getDistribution().getVersion();
            }
            if (!processedImages.contains(imageName)) {
              processingFunction.call(new PairTuple<String, DockerFacade>(imageName, dockerFacade));
              processedImages.add(imageName);
            }
          }
        }
      }
    }
  }

}
