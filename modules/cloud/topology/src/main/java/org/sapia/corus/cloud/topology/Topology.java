package org.sapia.corus.cloud.topology;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

/**
 * Corresponds to the <code>topology</code> element.
 * 
 * @author yduchesne
 *
 */
public class Topology implements XmlStreamable, Validateable {
  
  private String application;
  
  private Set<EnvTemplate>     envTemplates     = new HashSet<>();
  private Set<MachineTemplate> machineTemplates = new HashSet<>();
  private Set<ClusterTemplate> clusterTemplates = new HashSet<>();
  private Set<RegionTemplate>  regionTemplates  = new HashSet<>();
  
  private Set<Env>             environments     = new HashSet<>();
  private boolean              rendered;
  
  public void setApplication(String application) {
    this.application = application;
  }
  
  public String getApplication() {
    return application;
  }
  
  public void addRegionTemplate(RegionTemplate regionTemplate) {
    if (!regionTemplates.add(regionTemplate)) {
      throw new IllegalArgumentException(String.format("Duplicate <region-template> child element %s under <topology> element %s", regionTemplate.getName(), application));
    }
  }
  
  public void addEnvTemplate(EnvTemplate envTemplate) {
    if (!envTemplates.add(envTemplate)) {
      throw new IllegalArgumentException(String.format("Duplicate <env-template> child element %s under <topology> element %s", envTemplate.getName(), application));
    }
  }
  
  public Set<EnvTemplate> getEnvTemplates() {
    return envTemplates;
  }
  
  public void addMachineTemplate(MachineTemplate machineTemplate) {
    if (!machineTemplates.add(machineTemplate)) {
      throw new IllegalArgumentException(String.format("Duplicate <machine-template> child element %s under <topology> element %s", machineTemplate.getName(), application));
    }
  }
  
  public Set<MachineTemplate> getMachineTemplates() {
    return machineTemplates;
  }

  public void addClusterTemplate(ClusterTemplate clusterTemplate) {
    if (!clusterTemplates.add(clusterTemplate)) {
      throw new IllegalArgumentException(String.format("Duplicate <cluster-template> child element %s under <topology> element %s", clusterTemplate.getName(), application));
    }
  }
  
  public Set<ClusterTemplate> getClusterTemplates() {
    return clusterTemplates;
  }
  
  public void addEnv(Env env) {
    if (!environments.add(env)) {
      throw new IllegalArgumentException(String.format("Duplicate <env> child element %s under <topology> element %s", env.getName(), application));
    }
  }
  
  public Set<Env> getEnvs() {
    return environments;
  }
  
  public void render() {
    TopologyContext context = new TopologyContext();
    context
      .addClusterTemplates(getClusterTemplates())
      .addEnvTemplates(getEnvTemplates())
      .addMachineTemplates(getMachineTemplates());
    
    for (Env env : environments) {
      env.render(context);
    }
  }
  
  /**
   * @param file a {@link java.io.File} to save this instance's content to.
   * @throws IOException if an I/O error occurs.
   */
  public void saveTo(File file) throws IOException {
    saveTo(new FileWriter(file));
  }
  
  /**
   * 
   * 
   * @param the {@link Writer} to output this instance's content to.
   * @throws IOException if an I/O error occurs.
   */
  public void saveTo(Writer writer) throws IOException {
    if (!rendered) {
      render();
      validate();
      rendered = true;
    }
    try {
      XmlStreamWriter xmlWriter = new XmlStreamWriter(writer, true);
      output(xmlWriter);
    } finally {
      writer.close();
    }
  }
  
  // --------------------------------------------------------------------------
  // Validateable
  
  @Override
  public void validate() throws IllegalArgumentException {
    if (application == null) {
      throw new IllegalArgumentException("application not set on topology");
    }
    if (environments.isEmpty()) {
      throw new IllegalArgumentException("No <env> child element(s) defined for <topology> element " + getApplication());
    }
    for (Env env : environments) {
      env.validate();
    }
  }
  
  // --------------------------------------------------------------------------
  // XmlStreameable
  
  @Override
  public void output(XmlStream stream) {
    stream.beginRootElement("topology");
    for (Env env : environments) {
      env.output(stream);
    }
    stream.endRootElement("topology");
  }
  
}
