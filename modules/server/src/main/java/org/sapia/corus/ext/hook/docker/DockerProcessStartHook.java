package org.sapia.corus.ext.hook.docker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.text.StrLookup;
import org.apache.commons.lang.text.StrSubstitutor;
import org.sapia.corus.client.common.CompositeStrLookup;
import org.sapia.corus.client.common.EnvVariableStrLookup;
import org.sapia.corus.client.common.LogCallback;
import org.sapia.corus.client.common.PropertiesStrLookup;
import org.sapia.corus.client.common.ToStringUtils;
import org.sapia.corus.client.services.deployer.dist.Property;
import org.sapia.corus.client.services.deployer.dist.StarterResult;
import org.sapia.corus.client.services.deployer.dist.StarterType;
import org.sapia.corus.client.services.deployer.dist.docker.DockerPortMapping;
import org.sapia.corus.client.services.deployer.dist.docker.DockerStarter;
import org.sapia.corus.client.services.deployer.dist.docker.DockerStarter.DockerStarterAttachment;
import org.sapia.corus.client.services.deployer.dist.docker.DockerVolumeMapping;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.docker.DockerFacade;
import org.sapia.corus.processor.hook.ProcessContext;
import org.sapia.corus.processor.hook.ProcessStartHook;
import org.springframework.beans.factory.annotation.Autowired;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

/**
 * Implements the {@link ProcessStartHook} interface in the context of docker.
 *
 * @author yduchesne
 *
 */
public class DockerProcessStartHook implements ProcessStartHook {

  @Autowired
  private ServerContext serverContext;

  @Autowired
  private DockerFacade dockerFacade;

  // --------------------------------------------------------------------------
  // Visible for testing

  public void setServerContext(ServerContext serverContext) {
    this.serverContext = serverContext;
  }

  public void setDockerFacade(DockerFacade dockerFacade) {
    this.dockerFacade = dockerFacade;
  }

  // --------------------------------------------------------------------------
  // ProcessStartHook interface

  @Override
  public boolean accepts(ProcessContext context) {
    return context.getProcess().getStarterType().equals(StarterType.DOCKER);
  }

  @Override
  public void start(ProcessContext context, StarterResult starterResult, LogCallback callback) throws IOException {
    DockerClient client = dockerFacade.getDockerClient();
    try {

      DockerStarterAttachment attachment = starterResult.getNotNull(
          DockerStarter.DOCKER_STARTER_ATTACHMENT,
          DockerStarterAttachment.class
      );

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
    
      Map<String, String> envVars = new HashMap<String, String>();
      for (Property p : starter.getEnvironment().getProperties()) {
        if (p.getValue() != null) {
          String value = substitutor.replace(p.getValue());
          vars.put(p.getName(), value);
          envVars.put(p.getName(), value);
        }
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

      ContainerCreation creation = client.createContainer(
          containerBuilder.build(),
          context.getProcess().getDistributionInfo().getProcessName() + "-" + context.getProcess().getProcessID()
      );

      String containerId = creation.id();
      client.startContainer(containerId);
      context.getProcess().setOsPid(containerId);
      callback.info(String.format("Created Docker container for process %s (container id: %s)", 
          ToStringUtils.toString(context.getProcess()) , containerId));
    } catch (InterruptedException | DockerException e) {
      throw new IOException("Could not start Docker container for process: " + ToStringUtils.toString(context.getProcess()), e);
    } finally {
      client.close();
    }
  }

}
