package org.sapia.corus.docker;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.common.CompositeStrLookup;
import org.sapia.corus.client.common.EnvVariableStrLookup;
import org.sapia.corus.client.common.PropertiesStrLookup;
import org.sapia.corus.client.common.ToStringUtils;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.common.log.PrefixedLogCallback;
import org.sapia.corus.client.services.deployer.dist.Property;
import org.sapia.corus.client.services.deployer.dist.StarterResult;
import org.sapia.corus.client.services.deployer.dist.docker.DockerPortMapping;
import org.sapia.corus.client.services.deployer.dist.docker.DockerStarter;
import org.sapia.corus.client.services.deployer.dist.docker.DockerVolumeMapping;
import org.sapia.corus.client.services.deployer.dist.docker.DockerStarter.DockerStarterAttachment;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.processor.hook.ProcessContext;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Streams;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;
import com.spotify.docker.client.messages.ProgressMessage;
import com.spotify.docker.client.messages.RemovedImage;

/**
 * Implementation of the {@link DockerClientFacade} based on the {@link DockerClient} interface.
 * 
 * @author yduchesne
 *
 */
public class SpotifyDockerClientFacade implements DockerClientFacade {
  
   private static final String DOCKER_PREFIX = "DOCKER >>";
   private Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());
  
   private ServerContext serverContext;
   private DockerClient  dockerClient;

  SpotifyDockerClientFacade(ServerContext serverContext, DockerClient dockerClient) {
    this.serverContext = serverContext;
    this.dockerClient  = dockerClient;
  }

  @Override
  public void loadImage(String imageName, InputStream imagePayload,
      LogCallback callback) {
    
    try {
      Assertions.isFalse(StringUtils.isBlank(imageName), "Docker image name passed in cannot be null or blank");
      Assertions.notNull(imagePayload, "Docker image payload passed in cannot be null or blank");      
      
      final LogCallback prefixedLogCallback = wrap(callback);
      log.info("Loading Docker image '" + imageName + "' into Docker daemon...");
      dockerClient.load(imageName, imagePayload, new ProgressHandler() {
        @Override
        public void progress(ProgressMessage msg) throws DockerException {
          if (msg.error() != null) {
            prefixedLogCallback.error(msg.toString());
          } else {
            prefixedLogCallback.debug(msg.toString());
          }
        }
      });
    } catch (Exception e) {
      throw new DockerFacadeException("System error loading Docker image '" + imageName + "' into Docker daemon", e);
    } finally {
      Streams.closeSilently(imagePayload);
    } 
  }
  
  @Override
  public InputStream saveImage(String imageName, LogCallback callback) {
    Assertions.isFalse(StringUtils.isBlank(imageName), "Docker image name passed in cannot be null or blank");
    
    final LogCallback prefixedLogCallback = wrap(callback);
    log.info("Saving Docker image '" + imageName + "' from Docker daemon...");
    prefixedLogCallback.info("Obtaining image: " + imageName);
    try {
      return dockerClient.save(imageName);
    } catch (Exception e) {
      throw new DockerFacadeException("System error saving Docker image '" + imageName + "' from Docker daemon", e);
    }
  }

  @Override
  public void pullImage(String imageName, final LogCallback callback) {
    Assertions.isFalse(StringUtils.isBlank(imageName), "Docker image name passed in cannot be null or blank");

    final LogCallback prefixedLogCallback = wrap(callback);
    try {
      log.info("Pulling Docker image '" + imageName + "' from remote registry...");

      dockerClient.pull(imageName, new ProgressHandler() {
        @Override
        public void progress(ProgressMessage msg) throws DockerException {
          if (msg.error() != null) {
            prefixedLogCallback.error(msg.toString());
          } else {
            prefixedLogCallback.debug(msg.toString());
          }
        }
      });

    } catch (Exception e) {
      throw new DockerFacadeException("System error pulling Docker image '" + imageName + "' from remote registry", e);
    }
  }
  
  @Override
  public void removeImage(String imageName, LogCallback callback) {
    Assertions.isFalse(StringUtils.isBlank(imageName), "Docker image name passed in cannot be null or blank");
    
    LogCallback prefixedLogCallback = wrap(callback);
    try {
      log.info("Removing Docker image '" + imageName + "' from local daemon...");

      List<RemovedImage> response = dockerClient.removeImage(imageName, true, false);

      if (response.isEmpty()) {
        log.warn("No Docker image removed from local daemon");
        prefixedLogCallback.error("No Docker image removed from local daemon");
      } else {
        for (RemovedImage ri: response) {
          log.info("Removed Docker image " + ri.toString());
          prefixedLogCallback.debug("Removed Docker image id " + ri.imageId());
        }
      }

    } catch (Exception e) {
      throw new DockerFacadeException("System error removing Docker image '" + imageName + "' from local daemon", e);
    }
  }

  @Override
  public void removeContainer(String containerId, LogCallback callback) {
    Assertions.isFalse(StringUtils.isBlank(containerId), "Docker container id passed in cannot be null or blank");

    LogCallback prefixedLogCallback = wrap(callback);
    try {
      log.info("Removing docker container '" + containerId + "'...");
      prefixedLogCallback.debug("Removing container " + containerId + "...");

      dockerClient.removeContainer(containerId, true);
      log.info("Docker container " + containerId + " removed");
      prefixedLogCallback.debug("Docker container " + containerId + " removed");

    } catch (Exception e) {
      prefixedLogCallback.error("Error removing docker container " + containerId + " ==> " + e.getMessage());
      throw new DockerFacadeException("System error removing Docker container '" + containerId + "' from local daemon", e);
    }
  }
  
  @Override
  public String startContainer(String imageName, LogCallback callback) {
    Assertions.isFalse(StringUtils.isBlank(imageName), "Docker image name passed in cannot be null or blank");
 
    LogCallback prefixedLogCallback = wrap(callback);
    try {
      log.info("Starting Docker container '" + imageName + "'...");
      prefixedLogCallback.debug("Starting Docker container " + imageName + "...");

      ContainerConfig.Builder containerConfig = ContainerConfig.builder();
      containerConfig.image(imageName);
      String containerId = dockerClient.createContainer(containerConfig.build()).id();
      dockerClient.startContainer(containerId);
      log.info("Docker container " + containerId + " started");
      prefixedLogCallback.debug("Docker container " + containerId + " started");
      return containerId;
    } catch (Exception e) {
      prefixedLogCallback.error("Error starting Docker container for image " + imageName + " ==> " + e.getMessage());
      throw new DockerFacadeException("System error starting Docker container  for image: '" + imageName + "' from local daemon", e);
    }
  }
  
  @Override
  public void stopContainer(String containerId, int timeoutSeconds, LogCallback callback) {
    Assertions.isFalse(StringUtils.isBlank(containerId), "Docker container id passed in cannot be null or blank");

    LogCallback prefixedLogCallback = wrap(callback);
    try {
      log.info("Stopping Docker container '" + containerId + "'...");
      prefixedLogCallback.debug("Stopping container " + containerId + "...");

      dockerClient.stopContainer(containerId, timeoutSeconds);
      log.info("Docker container " + containerId + " stopped");
      prefixedLogCallback.debug("Docker container " + containerId + " stopped");

    } catch (Exception e) {
      prefixedLogCallback.error("Error stopping docker container " + containerId + " ==> " + e.getMessage());
      throw new DockerFacadeException("System error stopping docker container '" + containerId + "' from local daemon", e);
    }
  }


  @Override
  public String startContainer(ProcessContext context, StarterResult starterResult,
      DockerStarterAttachment attachment, LogCallback callback) throws IOException {
    try {
      return doStartContainer(context, starterResult, attachment, callback);
    } catch (InterruptedException e) {
      throw new IOException("Thread interrupted while attempting to start Docker container", e);
    } catch (DockerException e) {
      throw new IOException("Docker error caught while attempting to start container", e);
    }
  }
      
  
  public String doStartContainer(ProcessContext context, StarterResult starterResult,
      DockerStarterAttachment attachment, LogCallback callback)
      throws DockerException, InterruptedException {

    Map<String, String> vars = new HashMap<String, String>();
    vars.put("user.dir", attachment.getEnv().getCommonDir());
    vars.put("corus.home", serverContext.getHomeDir());

    Property[] envProperties = attachment.getEnv().getProperties();

    CompositeStrLookup propContext = new CompositeStrLookup()
        .add(StrLookup.mapLookup(vars))
        .add(PropertiesStrLookup.getInstance(envProperties))
        .add(PropertiesStrLookup.getSystemInstance())
        .add(new EnvVariableStrLookup());

    StrSubstitutor substitutor = new StrSubstitutor(propContext);

    DockerStarter           starter           = attachment.getStarter();
    ContainerConfig.Builder containerBuilder  = ContainerConfig.builder();
    HostConfig.Builder      hostConfigBuilder = HostConfig.builder();

    // user
    if (starter.getUser().isSet()) {
      containerBuilder.user(substitutor.replace(starter.getUser().get()));
    }

    // image
    if (starter.getImage().isSet()) {
      containerBuilder.image(substitutor.replace(starter.getImage().get()));
    } else {
      String image =
          context.getProcess().getDistributionInfo().getName() + ":"
          + context.getProcess().getDistributionInfo().getVersion();
      containerBuilder.image(image);
    }

    // cmd
    if (!starterResult.getCommand().isEmpty()) {
      String[] cmdArr = starterResult.getCommand().toArray();
      for (int i = 0; i < cmdArr.length; i++) {
        cmdArr[i] = substitutor.replace(cmdArr[i]);
      }
      containerBuilder.cmd(cmdArr);
    }

    // volumes
    if (!starter.getVolumeMappings().isEmpty()) {
      Set<String> volumes   = new HashSet<String>();
      List<String> bindings = new ArrayList<String>();
      for (DockerVolumeMapping m : starter.getVolumeMappings()) {
        String vol = m.getContainerVolume();
        if (m.getPermission().isSet()) {
          vol = vol + ":" + m.getPermission().get();
        }
        volumes.add(substitutor.replace(vol));
        bindings.add(m.getHostVolume() + ":" + substitutor.replace(m.getContainerVolume()));
      }
      containerBuilder.volumes(volumes);
      hostConfigBuilder.binds(bindings);
    }

    // mac
    if (starter.getMacAddress().isSet()) {
      containerBuilder.macAddress(substitutor.replace(starter.getMacAddress().get()));
    }

    // port bindings
    Map<String, List<PortBinding>> portBindings = new HashMap<String, List<PortBinding>>();
    Set<String> containerPorts = new HashSet<String>();
    for (DockerPortMapping portMapping : starter.getPortMappings()) {
      List<PortBinding> hostPorts = new ArrayList<PortBinding>();
      hostPorts.add(
          PortBinding.of(serverContext.getCorusHost().getEndpoint().getServerTcpAddress().getHost(),
          substitutor.replace(portMapping.getHostPort()))
      );
      portBindings.put(portMapping.getContainerPort(), hostPorts);
      containerPorts.add(portMapping.getContainerPort());
    }
    hostConfigBuilder.portBindings(portBindings);
    containerBuilder.exposedPorts(containerPorts);

    // cpu
    if (starter.getCpuShares().isSet()) {
      hostConfigBuilder.cpuShares(Long.parseLong(substitutor.replace(starter.getCpuShares().get())));
    }
    if (starter.getCpuSetCpus().isSet()) {
      hostConfigBuilder.cpusetCpus(substitutor.replace(starter.getCpuSetCpus().get()));
    }

    // memory
    if (starter.getMemory().isSet()) {
      containerBuilder.memory(Long.parseLong(substitutor.replace(starter.getMemory().get())));
    }
    if (starter.getMemorySwap().isSet()) {
      containerBuilder.memorySwap(Long.parseLong(substitutor.replace(starter.getMemorySwap().get())));
    }

    // working dir
    containerBuilder.workingDir(substitutor.replace(context.getProcess().getProcessDir()));

    // cgroup parent
    if (starter.getCgroupParent().isSet()) {
      hostConfigBuilder.cgroupParent(substitutor.replace(starter.getCgroupParent().get()));
    }

    ContainerCreation creation = dockerClient.createContainer(
        containerBuilder.build(),
        context.getProcess().getDistributionInfo().getProcessName() + "-" + context.getProcess().getProcessID()
    );

    String containerId = creation.id();
    dockerClient.startContainer(containerId);
    
    callback.debug(
        String.format(
          "Created Docker container for process %s (container id: %s)",
          ToStringUtils.toString(context.getProcess()) , containerId
        )
    );    
    return containerId;
  }
  
  private LogCallback wrap(LogCallback callback) {
    return new PrefixedLogCallback(DOCKER_PREFIX, callback);
  }

}
