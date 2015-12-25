package org.sapia.corus.ext.hook.docker;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.common.PairTuple;
import org.sapia.corus.client.common.ToStringUtils;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.configurator.Tag;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.deployer.dist.Starter;
import org.sapia.corus.client.services.deployer.dist.docker.DockerStarter;
import org.sapia.corus.deployer.processor.DeploymentContext;
import org.sapia.corus.deployer.processor.DeploymentPostProcessor;
import org.sapia.corus.deployer.processor.UndeploymentPostProcessor;
import org.sapia.corus.docker.DockerClientFacade;
import org.sapia.corus.docker.DockerFacade;
import org.sapia.corus.docker.DockerFacadeException;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class holds logic for synchronizing deployment/undeployment with Docker.
 *
 * @author yduchesne
 *
 */
public class DockerPostDeploymentProcessor implements DeploymentPostProcessor, UndeploymentPostProcessor {
  
  private Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());

  @Autowired
  private Configurator configurator;

  
  @Autowired
  private DockerFacade dockerFacade;

  // --------------------------------------------------------------------------
  // Visible for testing
  
  void setConfigurator(Configurator configurator) {
    this.configurator = configurator;
  }
  
  void setDockerFacade(DockerFacade dockerFacade) {
    this.dockerFacade = dockerFacade;
  }

  // --------------------------------------------------------------------------
  // DeploymentPostProcessor interface

  @Override
  public void onPostDeploy(final DeploymentContext context, final LogCallback callback) {
    onPostProcess(context, callback, new Func<Void, PairTuple<String, DockerClientFacade>>() {
      @Override
      public Void call(PairTuple<String, DockerClientFacade> data) {
        callback.debug(String.format(
            "Removing Docker image %s in the context of distribution deployment for: %s",
            data.getLeft(), ToStringUtils.toString(context.getDistribution())
        ));

        try {
          data.getRight().pullImage(data.getLeft(), callback);
        } catch (DockerFacadeException | IOException e) {
          log.error("Error trying to sync with Docker in post-deploy", e);
          callback.error(e.getMessage());
        }
        return null;
      }
    });
  }

  // --------------------------------------------------------------------------
  // UndeploymentPostProcessor interface

  @Override
  public void onPostUndeploy(final DeploymentContext context, final LogCallback callback)
      throws Exception {
    onPostProcess(context, callback, new Func<Void, PairTuple<String, DockerClientFacade>>() {
      @Override
      public Void call(PairTuple<String, DockerClientFacade> data) {
        callback.debug(String.format(
            "Removing Docker image %s in the context distribution undeployment for: %s",
            data.getLeft(), ToStringUtils.toString(context.getDistribution())
        ));

        try {
          data.getRight().removeImage(data.getLeft(), callback);
        } catch (DockerFacadeException | IOException e) {
          log.error("Error trying to sync with Docker in post-undeploy", e);
          callback.error(e.getMessage());
        }
        return null;
      }
    });
  }

  // --------------------------------------------------------------------------
  // Restricted

  private void onPostProcess(
      DeploymentContext context, 
      LogCallback callback, 
      Func<Void, PairTuple<String, DockerClientFacade>> processingFunction) {
    DockerClientFacade dockerClient = dockerFacade.getDockerClient();
    onPostProcess(context, callback, processingFunction, dockerClient);
  }
  
  private void onPostProcess(
      DeploymentContext context, 
      LogCallback callback, 
      Func<Void, PairTuple<String, DockerClientFacade>> processingFunction, 
      DockerClientFacade client) {

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
        for (Starter st : pc.getStarters()) {
          if (st instanceof DockerStarter) {
            if (!processTags.isEmpty()) {
              callback.info(
                  String.format("Process tags (%s) matched Corus tags, will proceed to deployment of Docker image",
                  ToStringUtils.joinToString(processTags))
              );
            }
            DockerStarter dst = (DockerStarter) st;
            String imageName = null;
            if (dst.getImage().isSet()) {
              imageName = dst.getImage().get();
            } else {
              imageName = context.getDistribution().getName() + ":" + context.getDistribution().getVersion();
            }
            if (!processedImages.contains(imageName)) {
              processingFunction.call(new PairTuple<String, DockerClientFacade>(imageName, client));
              processedImages.add(imageName);
            }
          }
        }
      }
    }
  }

}
