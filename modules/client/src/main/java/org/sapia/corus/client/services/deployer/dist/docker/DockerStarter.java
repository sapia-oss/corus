package org.sapia.corus.client.services.deployer.dist.docker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.sapia.console.CmdLine;
import org.sapia.corus.client.common.Env;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.exceptions.misc.MissingDataException;
import org.sapia.corus.client.services.deployer.dist.Dependency;
import org.sapia.corus.client.services.deployer.dist.Starter;
import org.sapia.corus.client.services.deployer.dist.StarterResult;
import org.sapia.corus.client.services.deployer.dist.StarterType;

/**
 * Implements a {@link Starter} for Docker.
 * 
 * @author yduchesne
 *
 */
public class DockerStarter implements Starter, Serializable {
  
  /**
   * A runtime attachement created when the {@link DockerStarter#toCmdLine(Env)} method is invoked.
   * @author yduchesne
   *
   */
  public static class DockerStarterAttachment {
    
    private Env           env;
    private DockerStarter starter;
    
    public DockerStarterAttachment(Env env, DockerStarter starter) {
      this.env     = env;
      this.starter = starter;
    }
    
    public Env getEnv() {
      return env;
    }
    
    public DockerStarter getStarter() {
      return starter;
    }
  
  }
  
  // ==========================================================================
  
  private static final long serialVersionUID = 1L;

  public static final String DEFAULT_NETWORK_MODE   = "host";

  public static final String DOCKER_STARTER_ATTACHMENT = "docker.starter.attachment";
  
  private String                  profile;
  
  private OptionalValue<String>   image          = OptionalValue.none();
  private String                  networkMode    = DEFAULT_NETWORK_MODE;
  private OptionalValue<String>   restartPolicy  = OptionalValue.none();
  private OptionalValue<String>   command        = OptionalValue.none();
  private OptionalValue<String>   addHost        = OptionalValue.none();
  private OptionalValue<String>   macAddress     = OptionalValue.none();
  
  private OptionalValue<String>   memory         = OptionalValue.none();
  private OptionalValue<String>   memorySwap     = OptionalValue.none();

  private OptionalValue<String>   cpuShares      = OptionalValue.none();
  private OptionalValue<String>   cpuPeriod      = OptionalValue.none();
  private OptionalValue<String>   cpuSetCpus     = OptionalValue.none();
  private OptionalValue<String>   cpuSetMems     = OptionalValue.none();
  private OptionalValue<Long>     cpuQuota       = OptionalValue.none();

  private OptionalValue<String>   cgroupParent     = OptionalValue.none();
  private OptionalValue<String>   blkioWeight      = OptionalValue.none();
  private OptionalValue<String>   oomKillDisable   = OptionalValue.none();
  private OptionalValue<String>   memorySwappiness = OptionalValue.none();
  private OptionalValue<String>   user             = OptionalValue.none();
   
  private boolean                 interopEnabled    = false;
  private boolean                 autoRemoveEnabled = true;

  private List<Dependency>          dependencies   = new ArrayList<Dependency>();
  private List<DockerPortMapping>   portMappings   = new ArrayList<DockerPortMapping>();
  private List<DockerVolumeMapping> volumeMappings = new ArrayList<DockerVolumeMapping>();
  private DockerEnv                 environment    = new DockerEnv();

  @Override
  public void setProfile(String profile) {
    this.profile = profile;
  }
  
  @Override
  public String getProfile() {
    return profile;
  }
  
  public void setImage(String image) {
    this.image = OptionalValue.of(image);
  }
  
  public OptionalValue<String> getImage() {
    return image;
  }
  
  public void setCommand(String command) {
    this.command = OptionalValue.of(command);
  }
  
  public OptionalValue<String> getCommand() {
    return command;
  }
  
  public void setAddHost(String addHost) {
    this.addHost = OptionalValue.of(addHost);
  }
  
  public OptionalValue<String> getAddHost() {
    return addHost;
  }
  
  public void setMacAddress(String macAddress) {
    this.macAddress = OptionalValue.of(macAddress);
  }
  
  public OptionalValue<String> getMacAddress() {
    return macAddress;
  }
  
  public void setNetworkMode(String networkMode) {
    this.networkMode = networkMode;
  }
  
  public String getNetworkMode() {
    return networkMode;
  }
  
  public void setRestartPolicy(String restartPolicy) {
    this.restartPolicy = OptionalValue.of(restartPolicy);
  }
  
  public OptionalValue<String> getRestartPolicy() {
    return restartPolicy;
  }
  
  public void setBlkioWeight(String blkioWeight) {
    this.blkioWeight = OptionalValue.of(blkioWeight);
  }
  
  public OptionalValue<String> getBlkioWeight() {
    return blkioWeight;
  }
  
  public void setCpuPeriod(String cpuPeriod) {
    this.cpuPeriod = OptionalValue.of(cpuPeriod);
  }
  
  public OptionalValue<String> getCpuPeriod() {
    return cpuPeriod;
  }
  
  public void setCpuQuota(Long cpuQuota) {
    this.cpuQuota = OptionalValue.of(cpuQuota);
  }
  
  public OptionalValue<Long> getCpuQuota() {
    return cpuQuota;
  }
  
  public void setCpuSetCpus(String cpuSetCpus) {
    this.cpuSetCpus = OptionalValue.of(cpuSetCpus);
  }
  
  public OptionalValue<String> getCpuSetCpus() {
    return cpuSetCpus;
  }
  
  public void setCpuSetMems(String cpuSetMems) {
    this.cpuSetMems = OptionalValue.of(cpuSetMems);
  }
  
  public OptionalValue<String> getCpuSetMems() {
    return cpuSetMems;
  }
  
  public void setCpuShares(String cpuShares) {
    this.cpuShares = OptionalValue.of(cpuShares);
  }
  
  public OptionalValue<String> getCpuShares() {
    return cpuShares;
  }
  
  public void setMemory(String memory) {
    this.memory = OptionalValue.of(memory);
  }
  
  public OptionalValue<String> getMemory() {
    return memory;
  }
  
  public void setMemorySwap(String memorySwap) {
    this.memorySwap = OptionalValue.of(memorySwap);
  }
  
  public OptionalValue<String> getMemorySwap() {
    return memorySwap;
  }
  
  public void setMemorySwappiness(String memorySwappiness) {
    this.memorySwappiness = OptionalValue.of(memorySwappiness);
  }
  
  public OptionalValue<String> getMemorySwappiness() {
    return memorySwappiness;
  }
  
  public void setOomKillDisable(String oomKillDisable) {
    this.oomKillDisable = OptionalValue.of(oomKillDisable);
  }
  
  public OptionalValue<String> getOomKillDisable() {
    return oomKillDisable;
  }
  
  public void setCgroupParent(String cgroupParent) {
    this.cgroupParent = OptionalValue.of(cgroupParent);
  }
  
  public OptionalValue<String> getCgroupParent() {
    return cgroupParent;
  }
  
  public void setUser(String user) {
    this.user = OptionalValue.of(user);
  }
  
  public OptionalValue<String> getUser() {
    return user;
  }
  
  public void addPortMapping(DockerPortMapping portMapping) {
    this.portMappings.add(portMapping);
  }
  
  public DockerPortMapping createPortMapping() {
    DockerPortMapping mapping = new DockerPortMapping();
    addPortMapping(mapping);
    return mapping;
  }
  
  public List<DockerPortMapping> getPortMappings() {
    return portMappings;
  }
  
  public DockerEnv getEnvironment() {
    return environment;
  }
  
  public DockerEnv createEnv() {
    return environment;
  }
  
  public void addVolumeMapping(DockerVolumeMapping volMapping) {
    this.volumeMappings.add(volMapping);
  }
  
  public DockerVolumeMapping createVolumeMapping() {
    DockerVolumeMapping mapping = new DockerVolumeMapping();
    addVolumeMapping(mapping);
    return mapping;
  }
  
  public List<DockerVolumeMapping> getVolumeMappings() {
    return volumeMappings;
  }
  
  /**
   * Adds a dependency to this instance.
   * 
   * @param dep
   *          a {@link Dependency}
   */
  public void addDependency(Dependency dep) {
    if (dep.getProfile() == null) {
      dep.setProfile(profile);
    }
    dependencies.add(dep);
  }

  /**
   * @return a new {@link Dependency} instance.
   */
  public Dependency createDependency() {
    Dependency dep = new Dependency();
    dep.setProfile(profile);
    addDependency(dep);
    return dep;
  }
  
  @Override
  public List<Dependency> getDependencies() {
    return dependencies;
  }
  
  /**
   * @param interopEnabled if <code>true</code>, indicates that interop is enabled (<code>true</code> by default).
   */
  public void setInteropEnabled(boolean interopEnabled) {
    this.interopEnabled = interopEnabled;
  }
  
  public boolean isInteropEnabled() {
    return interopEnabled;
  }
  
  /**
   * 
   * @param autoRemoveEnabled if <code>true</code>, indicates that auto-removal of the corresponding Docker image should be 
   * performed at undeployment.
   */
  public void setAutoRemoveEnabled(boolean autoRemoveEnabled) {
    this.autoRemoveEnabled = autoRemoveEnabled;
  }
  
  public boolean isAutoRemoveEnabled() {
    return this.autoRemoveEnabled;
  }
  
  @Override
  public StarterResult toCmdLine(Env env) throws MissingDataException {
    StarterResult result = new StarterResult(StarterType.DOCKER, command.isNull() ? new CmdLine() : CmdLine.parse(command.get()), interopEnabled);
    result.set(DOCKER_STARTER_ATTACHMENT, new DockerStarterAttachment(env,  this));
    return result;
  }

}
