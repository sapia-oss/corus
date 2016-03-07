package org.sapia.corus.cloud.topology;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.sapia.util.xml.ProcessingException;
import org.sapia.util.xml.confix.Dom4jProcessor;
import org.sapia.util.xml.confix.ReflectionFactory;

/**
 * Corresponds to the <code>topology</code> element.
 * 
 * @author yduchesne
 *
 */
public class Topology extends ParamContainer implements XmlStreamable, Validateable {
 
  
  private String org, application;
  private String version = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
  
  private Set<EnvTemplate>     envTemplates     = new HashSet<>();
  private Set<MachineTemplate> machineTemplates = new HashSet<>();
  private Set<ClusterTemplate> clusterTemplates = new HashSet<>();
  private Set<RegionTemplate>  regionTemplates  = new HashSet<>();
  
  private Set<Env>             environments     = new HashSet<>();
  private boolean              rendered;
  
  public void setOrg(String org) {
    this.org = org;
  }
  
  public String getOrg() {
    return org;
  }
  
  public void setApplication(String application) {
    this.application = application;
  }
  
  public String getApplication() {
    return application;
  }
  
  public void setVersion(String version) {
    this.version = version;
  }
  
  public String getVersion() {
    return version;
  }
  
  public void addRegionTemplate(RegionTemplate regionTemplate) {
    if (!regionTemplates.add(regionTemplate)) {
      throw new IllegalArgumentException(String.format("Duplicate <region-template> child element %s under <topology> element %s", regionTemplate.getName(), application));
    }
  }
  
  public Set<RegionTemplate> getRegionTemplates() {
    return regionTemplates;
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
    env.setTopology(this);
  }
  
  public Set<Env> getEnvs() {
    return environments;
  }
  
  /**
   * @param name an environment name.
   * @return the {@link Env} instance with the given name.
   * @throws IllegalArgumentException if no such environment is defined.
   */
  public Env getEnvByName(String name) throws IllegalArgumentException {
    for (Env e  : environments) {
      if (e.getName().equals(name)) {
        return e;
      }
    }
    throw new IllegalArgumentException("No environment with given name: " + name);
  }
  
  public void render() {
    TopologyContext context = new TopologyContext();
    context
      .addClusterTemplates(getClusterTemplates())
      .addEnvTemplates(getEnvTemplates())
      .addMachineTemplates(getMachineTemplates())
      .addRegionTemplates(getRegionTemplates());
    
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
  
  /**
   * @param is the {@link File} corresponding to the topology definition to load.
   * @return the {@link Topology} instance corresponding to the given file.
   */
  public static Topology newInstance(File toLoad) {
    try {
      return newInstance(new FileInputStream(toLoad));
    } catch (FileNotFoundException e) {
      throw new IllegalStateException("Could not find topology file: " + toLoad.getAbsolutePath());
    } 
  }
  
  /**
   * @param resource the path to the resource to load.
   * @return the {@link Topology} instance corresponding to the given resource.
   */
  public static Topology newInstance(String resource) {
    InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
    if (stream == null) {
      throw new IllegalStateException("Could not find topology resource: " + resource);
    }
    return newInstance(stream);
  }
  
  /**
   * Loads the {@link Topology} corresponding to the given topology definition stream. Closes
   * the stream before returning.
   * 
   * @param is the {@link InputStream} corresponding to the topology definition to load.
   * @return the {@link Topology} instance corresponding to the given stream.
   */
  public static Topology newInstance(InputStream is) {
    ReflectionFactory fac = new TopologyObjectFactory();
    Dom4jProcessor proc = new Dom4jProcessor(fac);

    try {
      return ((Topology) proc.process(is));
    } catch (ProcessingException e) {
      throw new IllegalStateException("Could not load topology", e);
    } finally {
      try {
        is.close();
      } catch (Exception e) {
        // noop
      }
    }
  }
  
  // --------------------------------------------------------------------------
  // Validateable
  
  @Override
  public void validate() throws IllegalArgumentException {
    if (org == null) {
      throw new IllegalArgumentException("'org' attribute not set on <topology> element");
    }
    if (application == null) {
      throw new IllegalArgumentException("'application' attribute not set on <topology> element");
    }
    if (environments.isEmpty()) {
      throw new IllegalArgumentException("No <env> child element(s) defined for <topology> element " + getApplication());
    }
    if (version == null) {
      throw new IllegalArgumentException("'version' attribute not set on <topology> element");
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
    stream.attribute("org", org);
    stream.attribute("application", application);
    stream.attribute("version", version);
    for (Param p : getParams()) {
      p.output(stream);
    }
    for (Env env : environments) {
      env.output(stream);
    }
    stream.endRootElement("topology");
  }
  
}
