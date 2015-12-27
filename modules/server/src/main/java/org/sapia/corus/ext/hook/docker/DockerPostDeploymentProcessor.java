package org.sapia.corus.ext.hook.docker;

import java.util.HashSet;
import java.util.Set;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.common.tuple.TripleTuple;
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
    if (dockerFacade.isRegistrySyncEnabled()) {
      onPostProcess(context, callback, new Func<Void, TripleTuple<String, DockerStarter, DockerClientFacade>>() {
        @Override
        public Void call(TripleTuple<String, DockerStarter, DockerClientFacade> data) {
          callback.info(String.format(
              "Pulling Docker image %s in the context of distribution deployment for: %s",
              data.getFirst(), ToStringUtil.toString(context.getDistribution())
          ));
          try {
            data.getThird().pullImage(data.getFirst(), callback);
          } catch (DockerFacadeException e) {
            log.error("Error trying to sync with Docker in post-deploy", e);
            callback.error(e.getMessage());
          }
          return null;
        }
      });
    } else {
      callback.info("Synchronization with Docker registry is disabled. Checking if images have been pre-deployed");
      Set<String> imageNames = new HashSet<>();
      for (ProcessConfig pc : context.getDistribution().getProcesses()) {
        for (Starter st : pc.getStarters()) {
          if (st instanceof DockerStarter) {
            DockerStarter dst = (DockerStarter) st;
            String imageName;
            if (dst.getImage().isSet()) {
              imageName = dst.getImage().get();
            } else {
              imageName = context.getDistribution().getName() + ":" + context.getDistribution().getVersion();
            }
            imageNames.add(imageName);
          }
        }
      }
      try {
        Set<String> notFound = dockerFacade.getDockerClient().checkContainsImages(imageNames);
        if (!notFound.isEmpty()) {
          String msg = "Could not find the following expected Docker images in Docker daemon: " + ToStringUtil.joinToString(notFound);
          log.error(msg);
          callback.error(msg);
        }
      } catch (DockerFacadeException e) {
        log.error("Error checking state of Docker daemon", e);
        callback.error(e.getMessage());
      }
    }
  }

  // --------------------------------------------------------------------------
  // UndeploymentPostProcessor interface

  @Override
  public void onPostUndeploy(final DeploymentContext context, final LogCallback callback)
      throws Exception {
    if (dockerFacade.isAutoRemoveEnabled()) {
      onPostProcess(context, callback, new Func<Void, TripleTuple<String, DockerStarter, DockerClientFacade>>() {
        @Override
        public Void call(TripleTuple<String, DockerStarter, DockerClientFacade> data) {
          if (data.getSecond().isAutoRemoveEnabled()) {
            callback.debug(String.format(
                "Removing Docker image %s in the context of distribution undeployment for: %s",
                data.getFirst(), ToStringUtil.toString(context.getDistribution())
            ));
            try {
              data.getThird().removeImage(data.getFirst(), callback);
            } catch (DockerFacadeException e) {
              log.error("Error trying to sync with Docker in post-undeploy", e);
              callback.error(e.getMessage());
            }
          } else {
            callback.debug(String.format(
                "Auto-removal is disabled for Docker image %s in the context of distribution undeployment for: %s",
                data.getFirst(), ToStringUtil.toString(context.getDistribution())
            ));
          }  
          return null;
        }
      });
    } else {
      callback.info(String.format(
          "Docker images corresponding to distribution %s will not be removed (auto-removal is disabled)", 
          ToStringUtil.toString(context.getDistribution())
      ));
    }
  }

  // --------------------------------------------------------------------------
  // Restricted

  private void onPostProcess(
      DeploymentContext context, 
      LogCallback callback, 
      Func<Void, TripleTuple<String, DockerStarter, DockerClientFacade>> processingFunction) {
    DockerClientFacade dockerClient = dockerFacade.getDockerClient();
    onPostProcess(context, callback, processingFunction, dockerClient);
  }
  
  private void onPostProcess(
      DeploymentContext context, 
      LogCallback callback, 
      Func<Void, TripleTuple<String, DockerStarter, DockerClientFacade>> processingFunction, 
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
            DockerStarter dst = (DockerStarter) st;
            String imageName = null;
            if (dst.getImage().isSet()) {
              imageName = dst.getImage().get();
            } else {
              imageName = context.getDistribution().getName() + ":" + context.getDistribution().getVersion();
            }
            if (!processTags.isEmpty()) {
              callback.info(
                  String.format("Process tags (%s) matched Corus tags, will process Docker image: %s",
                  ToStringUtil.joinToString(processTags), imageName)
              );
            }
            if (!processedImages.contains(imageName)) {
              processingFunction.call(new TripleTuple<String, DockerStarter, DockerClientFacade>(imageName, dst, client));
              processedImages.add(imageName);
            }
          }
        }
      }
    }
  }

}
