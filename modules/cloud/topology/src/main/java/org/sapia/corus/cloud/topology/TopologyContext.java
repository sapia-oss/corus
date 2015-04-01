package org.sapia.corus.cloud.topology;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Holds references to the configured {@link EnvTemplate}, {@link MachineTemplate}s, etc.
 * 
 * @author yduchesne
 *
 */
public class TopologyContext {

  private Map<String, EnvTemplate>     envTemplates     = new HashMap<>();
  private Map<String, MachineTemplate> machineTemplates = new HashMap<>();
  private Map<String, ClusterTemplate> clusterTemplates = new HashMap<>();
  private Map<String, RegionTemplate>  regionTemplates  = new HashMap<>();
  
  public TopologyContext() {
  }
  
  public TopologyContext addEnvTemplates(Collection<EnvTemplate> envTemplates) {
    for (EnvTemplate t : envTemplates) {
      addEnvTemplate(t);
    }
    return this;
  }
  
  public TopologyContext addEnvTemplate(EnvTemplate t) {
    if (envTemplates.containsKey(t.getName())) {
      throw new IllegalArgumentException("Duplicate environment template for name: " + t.getName());
    }
    envTemplates.put(t.getName(), t);
    return this;
  }
  
  public TopologyContext addMachineTemplates(Collection<MachineTemplate> machineTemplates) {
    for (MachineTemplate t : machineTemplates) {
      addMachineTemplate(t);
    }
    return this;
  }
  
  public TopologyContext addMachineTemplate(MachineTemplate t) {
    if (machineTemplates.containsKey(t.getName())) {
      throw new IllegalArgumentException("Duplicate machine template for name: " + t.getName());
    }
    machineTemplates.put(t.getName(), t);
    return this;
  }
  
  public TopologyContext addClusterTemplates(Collection<ClusterTemplate> clusterTemplates) {
    for (ClusterTemplate t : clusterTemplates) {
      addClusterTemplate(t);
    }
    return this;
  }
  
  public TopologyContext addClusterTemplate(ClusterTemplate t) {
    if (clusterTemplates.containsKey(t.getName())) {
      throw new IllegalArgumentException("Duplicate cluster template for name: " + t.getName());
    }
    clusterTemplates.put(t.getName(), t);
    return this;
  }
  
  public TopologyContext addRegionTemplates(Collection<RegionTemplate> regionTemplates) {
    for (RegionTemplate t : regionTemplates) {
      addRegionTemplate(t);
    }
    return this;
  }
  
  public TopologyContext addRegionTemplate(RegionTemplate t) {
    if (regionTemplates.containsKey(t.getName())) {
      throw new IllegalArgumentException("Duplicate region template for name: " + t.getName());
    }
    regionTemplates.put(t.getName(), t);
    return this;
  }
  
  public EnvTemplate resolveEnvTemplate(String ref) throws IllegalArgumentException {
    EnvTemplate t = envTemplates.get(ref);
    if (t == null) {
      throw new IllegalArgumentException("No environment template found for: " + ref);
    }
    return t;
  }
  
  public MachineTemplate resolveMachineTemplate(String ref) throws IllegalArgumentException {
    MachineTemplate t = machineTemplates.get(ref);
    if (t == null) {
      throw new IllegalArgumentException("No machine template found for: " + ref);
    }
    return t;
  }
  
  public ClusterTemplate resolveClusterTemplate(String ref) throws IllegalArgumentException {
    ClusterTemplate t = clusterTemplates.get(ref);
    if (t == null) {
      throw new IllegalArgumentException("No cluster template found for: " + ref);
    }
    return t;
  }
  
  public RegionTemplate resolveRegionTemplate(String ref) throws IllegalArgumentException {
    RegionTemplate t = regionTemplates.get(ref);
    if (t == null) {
      throw new IllegalArgumentException("No region template found for: " + ref);
    }
    return t;
  }
  
  public static TopologyContext newInstance() {
    return new TopologyContext();
  }
}
