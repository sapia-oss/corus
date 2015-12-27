package org.sapia.corus.docker;

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
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.CompositeStrLookup;
import org.sapia.corus.client.common.EnvVariableStrLookup;
import org.sapia.corus.client.common.PropertiesStrLookup;
import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.client.common.log.PrefixedLogCallback;
import org.sapia.corus.client.services.deployer.dist.Property;
import org.sapia.corus.client.services.deployer.dist.StarterResult;
import org.sapia.corus.client.services.deployer.dist.docker.DockerPortMapping;
import org.sapia.corus.client.services.deployer.dist.docker.DockerStarter;
import org.sapia.corus.client.services.deployer.dist.docker.DockerStarter.DockerStarterAttachment;
import org.sapia.corus.client.services.deployer.dist.docker.DockerVolumeMapping;
import org.sapia.corus.client.services.docker.DockerContainer;
import org.sapia.corus.client.services.docker.DockerImage;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.processor.hook.ProcessContext;
import org.sapia.corus.util.DynamicProperty;
import org.sapia.corus.util.docker.DockerImageName;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;
import org.sapia.ubik.util.Streams;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.DockerClient.ListImagesParam;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.ProgressHandler;
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.Image;
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
  
  private static final int DEFAULT_SECONDS_BEFORE_KILL = 30;
  
   private static final String DOCKER_PREFIX = "DOCKER >>";
   private Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());
  
   private ServerContext            serverContext;
   private DockerClient             dockerClient;
   private DynamicProperty<Boolean> registrySyncEnabled;

  SpotifyDockerClientFacade(ServerContext serverContext, DockerClient dockerClient, DynamicProperty<Boolean> registrySyncEnabled) {
    this.serverContext       = serverContext;
    this.dockerClient        = dockerClient;
    this.registrySyncEnabled = registrySyncEnabled;
  }

  @Override
  public void loadImage(String imageName, InputStream imagePayload,
      LogCallback callback) {
    Assertions.isFalse(StringUtils.isBlank(imageName), "Docker image name passed in cannot be null or blank");
    Assertions.notNull(imagePayload, "Docker image payload passed in cannot be nullk");

    final LogCallback prefixedLogCallback = wrap(callback);

    try {
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
    Assertions.illegalState(!registrySyncEnabled.getValue(), "Cannot pull image from Docker registry: registry synchronization is disabled");
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
      throw new DockerFacadeException("System error starting Docker container for image: '" + imageName + "' from local daemon", e);
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
      throw new DockerFacadeException("System error stopping docker container '" + containerId + "' from local daemon", e);
    }
  }


  @Override
  public String startContainer(ProcessContext context, StarterResult starterResult,
      DockerStarterAttachment attachment, LogCallback callback) throws DockerFacadeException {
    try {
      LogCallback prefixedLogCallback = wrap(callback);
      return doStartContainer(context, starterResult, attachment, prefixedLogCallback);
    } catch (InterruptedException e) {
      throw new DockerFacadeException("Thread interrupted while attempting to start Docker container", e);
    } catch (DockerException e) {
      throw new DockerFacadeException("Docker error caught while attempting to start container", e);
    }
  }
  
  @Override
  public boolean containsImage(String imageName) throws DockerFacadeException {
    Assertions.isFalse(StringUtils.isBlank(imageName), "Docker container id passed in cannot be null or blank");
    try {
      String normalizedImageName = DockerImageName.parse(imageName).toString();
      for (Image img : dockerClient.listImages()) {
        for (String t : img.repoTags()) {
          if (normalizedImageName.equals(t)) {
            return true;
          }
        }
      }
      return false;
    } catch (InterruptedException e) {
      throw new DockerFacadeException("Thread interrupted while attempting to check Docker images", e);
    } catch (DockerException e) {
      throw new DockerFacadeException("Docker error caught while attempting to check Docker images", e);
    }
  }
  
  @Override
  public Set<String> checkContainsImages(Set<String> imageNames) throws DockerFacadeException {
    Set<String> normalizedImageNames = Collects.convertAsSet(imageNames, new Func<String, String>() {
      @Override
      public String call(String imageName) {
        return DockerImageName.parse(imageName).toString();
      }
    });
    Set<String> notFound = new HashSet<>(normalizedImageNames);
    try {
      for (Image img : dockerClient.listImages()) {
        for (String expected : normalizedImageNames) {
          for (String t : img.repoTags()) {
            if (expected.equals(t)) {
              notFound.remove(t);
              break;
            }
          }
        }
      }
      return notFound;
    } catch (InterruptedException e) {
      throw new DockerFacadeException("Thread interrupted while attempting to check Docker images", e);
    } catch (DockerException e) {
      throw new DockerFacadeException("Docker error caught while attempting to check Docker images", e);
    }
  }
  
  @Override
  public List<DockerImage> listImages(ArgMatcher tagMatcher) throws DockerFacadeException {
    List<DockerImage> matched = new ArrayList<>();
    try {
      for (Image img : dockerClient.listImages(ListImagesParam.allImages(false))) {
        for (String t : img.repoTags()) {
          if (tagMatcher.matches(t)) {
            DockerImage dimg = new DockerImage(img.id(), img.created());
            dimg.getTags().addAll(img.repoTags());
            matched.add(dimg);
            break;
          }
        }
      }
      return matched;
    } catch (InterruptedException e) {
      throw new DockerFacadeException("Thread interrupted while attempting to check Docker images", e);
    } catch (DockerException e) {
      throw new DockerFacadeException("Docker error caught while attempting to check Docker images", e);
    }
  }
  
  @Override
 public List<DockerContainer> listContainers(ArgMatcher nameMatcher) throws DockerFacadeException {
    List<DockerContainer> matched = new ArrayList<>();
    try {
      for (Container cnt : dockerClient.listContainers(ListContainersParam.allContainers(false))) {
        log.debug("Got container: " + cnt);
        if(nameMatcher.matches(cnt.image())) {
          matched.add(convert(cnt));
        } else {
          for (String n : cnt.names()) {
            if (nameMatcher.matches(n)) {
              matched.add(convert(cnt));
              break;
            }
          }
        }
      }
      return matched;
    } catch (InterruptedException e) {
      throw new DockerFacadeException("Thread interrupted while attempting to check Docker containers", e);
    } catch (DockerException e) {
      throw new DockerFacadeException("Docker error caught while attempting to check Docker containers", e);
    }
 }
  
  private String doStartContainer(ProcessContext context, StarterResult starterResult,
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

    // ------------------------------------------------------------------------
    // Config touching both host and container
    
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

    // ------------------------------------------------------------------------
    // Host-centric
    
    // cpu
    if (starter.getCpuShares().isSet()) {
      hostConfigBuilder.cpuShares(Long.parseLong(substitutor.replace(starter.getCpuShares().get())));
    }
    if (starter.getCpuSetCpus().isSet()) {
      hostConfigBuilder.cpusetCpus(substitutor.replace(starter.getCpuSetCpus().get()));
    }
    if (starter.getCpuQuota().isSet()) {
      hostConfigBuilder.cpuQuota(starter.getCpuQuota().get());
    }
      
    // memory
    if (starter.getMemory().isSet()) {
      hostConfigBuilder.memory(Long.parseLong(substitutor.replace(starter.getMemory().get())));
    }
    if (starter.getMemorySwap().isSet()) {
      hostConfigBuilder.memorySwap(Long.parseLong(substitutor.replace(starter.getMemorySwap().get())));
    }

    // cgroup parent
    if (starter.getCgroupParent().isSet()) {
      hostConfigBuilder.cgroupParent(substitutor.replace(starter.getCgroupParent().get()));
    }
    
    // ------------------------------------------------------------------------
    // Container-centric
    
    // env
    if (!starter.getEnvironment().getProperties().isEmpty()) {
      List<String> env = new ArrayList<String>();
      for (Property envProp : starter.getEnvironment().getProperties()) {
        String envValue =  substitutor.replace(envProp.getValue());
        env.add(envProp.getName() + "=" + envValue);
      }
      containerBuilder.env(env);
    }

    // mac
    if (starter.getMacAddress().isSet()) {
      containerBuilder.macAddress(substitutor.replace(starter.getMacAddress().get()));
    }
    
    // user
    if (starter.getUser().isSet()) {
      containerBuilder.user(substitutor.replace(starter.getUser().get()));
    }

    // image
    String image = null;
    if (starter.getImage().isSet()) {
      image = substitutor.replace(starter.getImage().get());
    } else {
      image =
          context.getProcess().getDistributionInfo().getName() + ":"
          + context.getProcess().getDistributionInfo().getVersion();
    }
    containerBuilder.image(image);

    // cmd
    if (!starterResult.getCommand().isEmpty()) {
      String[] cmdArr = starterResult.getCommand().toArray();
      for (int i = 0; i < cmdArr.length; i++) {
        cmdArr[i] = substitutor.replace(cmdArr[i]);
      }
      containerBuilder.cmd(cmdArr);
    }
    
    // working dir
    containerBuilder.workingDir(substitutor.replace(context.getProcess().getProcessDir()));

    // assigning host config
    containerBuilder.hostConfig(hostConfigBuilder.build());
    
    // creating container
    ContainerCreation creation = null;
    
    try {
      creation = dockerClient.createContainer(
          containerBuilder.build(),
          image.replace("/", "-").replace(":", "-").replace(".", "_")
          + "-" + context.getProcess().getDistributionInfo().getProcessName() + "-" + context.getProcess().getProcessID()
      );
  
      String containerId = creation.id();
      dockerClient.startContainer(containerId);
      
      callback.info(
          String.format(
            "Created Docker container for process %s (container id: %s, image: %s)",
            ToStringUtil.toString(context.getProcess()) , containerId, image
          )
      );    
      return containerId;
    } catch (DockerException e) {
      log.error("Could not start container for image: " + image, e);
      callback.error(String.format("Could not start container for image: %s (%s)", image, e.getMessage()));
      if (creation != null) {
        callback.info("Will try stopping container " + creation.id());
        try {
          dockerClient.stopContainer(creation.id(), DEFAULT_SECONDS_BEFORE_KILL);
        } catch (DockerException e2) {
          callback.error(String.format("Could not stop container %s (%s)", creation.id(), e2.getMessage()));
          log.error("Could not stop container " + creation.id(), e2);
        }
      }
      throw e;
    }
  }
  
  private LogCallback wrap(LogCallback callback) {
    return new PrefixedLogCallback(DOCKER_PREFIX, callback);
  }
  
  private DockerContainer convert(Container cnt) {
    DockerContainer dcnt = new DockerContainer(cnt.id(), cnt.image(), Long.toString(cnt.created()));
    dcnt.getNames().addAll(cnt.names());
    return dcnt;
  }

}
