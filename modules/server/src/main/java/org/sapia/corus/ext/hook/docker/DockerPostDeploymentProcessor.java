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
import org.springframework.beans.factory.annotation.Autowired;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.messages.ProgressMessage;

/**
 * This class holds logic for synchronizing deployment/undeployment with Docker.
 * 
 * @author yduchesne
 *
 */
public class DockerPostDeploymentProcessor implements DeploymentPostProcessor, UndeploymentPostProcessor {
  
  @Autowired
  private Configurator configurator;
  
  @Autowired
  private DockerFacade dockerFacade;

  // --------------------------------------------------------------------------
  // Visible for testing
  
  public void setConfigurator(Configurator configurator) {
    this.configurator = configurator;
  }
  
  public void setDockerFacade(DockerFacade dockerFacade) {
    this.dockerFacade = dockerFacade;
  }
  
  // --------------------------------------------------------------------------
  // DeploymentPostProcessor interface
  
  @Override
  public void onPostDeploy(final DeploymentContext context, final LogCallback callback) {
    onPostProcess(context, callback, new Func<Void, PairTuple<String,DockerClient>>() {
      @Override
      public Void call(PairTuple<String, DockerClient> data) {
        callback.debug(String.format(
            "Removing Docker image %s in the context of distribution deployment for: %s", 
            data.getLeft(), ToStringUtils.toString(context.getDistribution())
        ));
        try {
          data.getRight().pull(data.getLeft(), new ProgressHandler() {
            @Override
            public void progress(ProgressMessage msg) throws DockerException {
              callback.debug(msg.progress());
            }
          });
          return null;
        } catch (InterruptedException | DockerException e) {
            String msg = String.format(
                "Error occured while synchronizing distribution %s with Docker", 
                ToStringUtils.toString(context.getDistribution())
            );
            throw new IllegalStateException(msg, e);
          }
        }
      
    });
  }
  
  // --------------------------------------------------------------------------
  // UndeploymentPostProcessor interface
  
  @Override
  public void onPostUndeploy(final DeploymentContext context, final LogCallback callback)
      throws Exception {
    onPostProcess(context, callback, new Func<Void, PairTuple<String,DockerClient>>() {
      @Override
      public Void call(PairTuple<String, DockerClient> data) {
        callback.debug(String.format(
            "Removing Docker image %s in the context distribution undeployment for: %s", 
            data.getLeft(), ToStringUtils.toString(context.getDistribution())
        ));
        try {
          data.getRight().removeImage(data.getLeft());
          return null;
        } catch (InterruptedException | DockerException e) {
            String msg = String.format(
                "Error occured while synchronizing distribution %s with Docker", 
                ToStringUtils.toString(context.getDistribution())
            );
            throw new IllegalStateException(msg, e);
          }
        }
      
    });
  }
  
  // --------------------------------------------------------------------------
  // Restricted
  
  private void onPostProcess(DeploymentContext context, LogCallback callback, Func<Void, PairTuple<String, DockerClient>> processingFunction) {
    DockerClient dockerClient = null;
    
    Set<String> processedImages = new HashSet<String>();
    Set<String> corusTags = Collects.convertAsSet(configurator.getTags(), new Func<String, Tag>() {
      @Override
      public String call(Tag t) {
        return t.getValue();
      }
    });

    try {
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
              if (dockerClient == null) {
                dockerClient = dockerFacade.getDockerClient();
              }
              String imageName = null;
              if (dst.getImage().isSet()) {
                imageName = dst.getImage().get();
              } else {
                imageName = context.getDistribution().getName() + ":" + context.getDistribution().getVersion();
              }
              if (!processedImages.contains(imageName)) {
                processingFunction.call(new PairTuple<String, DockerClient>(imageName, dockerClient));
                processedImages.add(imageName);
              }
            }
          }
        }
      }
    } finally {
      if (dockerClient != null) {
        dockerClient.close();
      }
    }
  }

}
