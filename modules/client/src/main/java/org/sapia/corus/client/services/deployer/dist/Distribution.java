package org.sapia.corus.client.services.deployer.dist;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.FileUtils;
import org.sapia.corus.client.common.Matcheable;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.corus.client.exceptions.deployer.DeploymentException;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.file.FileSystemModule;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;
import org.sapia.util.xml.ProcessingException;
import org.sapia.util.xml.confix.ConfigurationException;
import org.sapia.util.xml.confix.Dom4jProcessor;
import org.sapia.util.xml.confix.ObjectCreationCallback;
import org.sapia.util.xml.confix.ReflectionFactory;

/**
 * This class corresponds to the <code>distribution</code> element in the
 * corus.xml file.
 * 
 * @author Yanick Duchesne
 */

public class Distribution implements java.io.Serializable, ObjectCreationCallback, Comparable<Distribution>, JsonStreamable, Matcheable {

  static final long serialVersionUID = 1L;
  
  public enum State {
    DEPLOYING,
    DEPLOYED
  }

  private static final String DEPLOYMENT_DESCRIPTOR = "META-INF/corus.xml";
  private String              name;
  private String              version;
  private String              baseDir;
  private String              commonDir;
  private String              processesDir;
  private String[]            tags;
  private String[]            categories;
  private List<ProcessConfig> processConfigs = new ArrayList<ProcessConfig>();
  private long                timestamp      = System.currentTimeMillis();
  private volatile State      state          = State.DEPLOYING;

  public Distribution() {
  }

  public Distribution(String name, String version) {
    this.name = name;
    this.version = version;
  }

  /**
   * Sets this distribution's name.
   * 
   * @param name
   *          a name.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Sets this instance's tags.
   * 
   * @param tagList
   */
  public void setTags(String tagList) {
    tags = tagList.split(",");
    for (int i = 0; i < tags.length; i++) {
      tags[i] = tags[i].trim();
    }
  }

  /**
   * @return the set of tags held by this instance.
   */
  public Set<String> getTagSet() {
    Set<String> set = new HashSet<String>();
    if (tags != null) {
      for (String t : tags) {
        set.add(t);
      }
    }
    return set;
  }
  
  /**
   * Sets this instance's categories.
   * 
   * @param categoryList a comma-delimited list of categories.
   */
  public void setPropertyCategories(String categoryList) {
    categories = categoryList.split(",");
    for (int i = 0; i < categories.length; i++) {
      categories[i] = categories[i].trim();
    }  
  }
  
  /**
   * @return the list of categories held by this instance.
   */
  public List<String> getPropertyCategories() {
    if (categories == null) {
      return new ArrayList<>(0);
    }
    return Collects.arrayToList(categories);
  }

  /**
   * Returns this distribution's name.
   * 
   * @return a name.
   */
  public String getName() {
    return name;
  }

  /**
   * Sets this distribution's version.
   * 
   * @param version
   *          a version.
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Returns this distribution's version.
   * 
   * @return a version as a string.
   */
  public String getVersion() {
    return version;
  }
  
  /**
   * @param timestamp a timestamp.
   */
  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }
  
  /**
   * @return this instance's timestamp.
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * Adds a process configuration to this distribution.
   * 
   * @param conf
   *          a {@link ProcessConfig} instance, representing a process
   *          configuration.
   */
  public void addProcess(ProcessConfig conf) {
    processConfigs.add(conf);
  }

  /**
   * Returns the process configurations of this distribution.
   * 
   * @return the list of {@link ProcessConfig} instances that this distribution
   *         holds.
   */
  public List<ProcessConfig> getProcesses() {
    return processConfigs;
  }

  /**
   * Tests for the presence of a process configuration within this distribution.
   * 
   * @param name
   *          the name of a process configuration to test for.
   * @return <code>true</code> if this instance contains a process configuration
   *         with the given name.
   */
  public boolean containsProcess(String name) {
    for (ProcessConfig pc : processConfigs) {
      if (pc.getName().equals(name)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the <code>ProcessConfig</code> that corresponds to the given name.
   * 
   * @param name
   *          the name of a process configuration to test for.
   * @return the {@link ProcessConfig} that corresponds to the given name, or
   *         <code>null</code> if none could be found.
   */
  public ProcessConfig getProcess(String name) {
    for (ProcessConfig pc : processConfigs) {
      if (name.matches(pc.getName())) {
        return pc;
      }
    }
    return null;
  }

  /**
   * @param name
   *          an {@link ArgMatcher} corresponding to the process name to match.
   * @return the {@link ProcessConfig}s whose name match the given argument.
   */
  public List<ProcessConfig> getProcesses(ArgMatcher name) {
    List<ProcessConfig> toReturn = new ArrayList<ProcessConfig>();

    for (ProcessConfig pc : processConfigs) {
      if (name.matches(pc.getName())) {
        toReturn.add(pc);
      }
    }
    return toReturn;
  }

  /**
   * Sets this distribution base directory.
   * 
   * @param baseDir
   *          this distribution's base dir. as a string.
   */
  public void setBaseDir(String baseDir) {
    this.baseDir = baseDir;
    commonDir    = FileUtils.toPath(baseDir, "common");
    processesDir = FileUtils.toPath(baseDir, "processes");
  }

  /**
   * Returns the base directory of this distribution.
   * 
   * @return the full path to the directory of this distribution under the Corus
   *         server.
   */
  public String getBaseDir() {
    return baseDir;
  }

  /**
   * Returns the process process directory.
   * 
   * @return the full path to the process process directory.
   */
  public String getProcessesDir() {
    return processesDir;
  }

  /**
   * Returns the common directory (the directory shared by all processes or the
   * directory where the distribution's archive was extracted).
   */
  public String getCommonDir() {
    return commonDir;
  }

  /**
   * Returns a name in the following format: <code>name</code>-
   * <code>version</code>.<code>zip</code>
   * 
   * @return the file name of the .zip file corresponding to the distribution
   *         that this instance represents.
   */
  public String getDistributionFileName() {
    return this.name + "-" + version + ".zip";
  }
  
  /**
   * @param state a {@link State} to assign to this instance.
   */
  public void setState(State state) {
    this.state = state;
  }
  
  /**
   * @return this instance's current state.
   */
  public State getState() {
    return state;
  }

  /**
   * @param criteria the {@link DistributionCriteria} to use for matching against this instance.
   * @return <code>true</code> if this instance matches the given criteria.
   */
  public boolean matches(DistributionCriteria criteria) {
    return criteria.getName().matches(name) && criteria.getVersion().matches(version);
  }
  
  @Override
  public boolean matches(Pattern pattern) {
     return pattern.matches(name) || pattern.matches(version) || matchesProcesses(pattern);
  }
  
  /**
   * Returns an instance of this class built from the <code>corus.xml</code>
   * configuration whose content is passed as the given <code>InputStream</code>
   * .
   * 
   * @param is
   *          an <code>InputStream</code> corresponding to a
   *          <code>corus.xml</code> configuration.
   * 
   * @throws DeploymentException
   *           if a problem occurs creating the <code>Distribution</code>
   *           object.
   */
  public static Distribution newInstance(InputStream is) throws DeploymentException {
    ReflectionFactory fac = new DeployerObjectFactory();
    Dom4jProcessor proc = new Dom4jProcessor(fac);

    try {
      return ((Distribution) proc.process(is));
    } catch (ProcessingException e) {
      throw new DeploymentException(e.getMessage(), e);
    }
  }

  /**
   * Returns an instance of this class built from the <code>corus.xml</code>
   * configuration whose file is in the <code>META-INF</code> directory of the
   * archive whose name is passed as a parameter.
   * 
   * @param zipFile
   *          the {@link File} corresponding to the zip file to get the
   *          configuration from.
   * @param the
   *          {@link FileSystemModule} abstracting the file system to read from.
   * 
   * @throws DeploymentException
   *           if a problem occurs creating the <code>Distribution</code>
   *           object.
   */
  public static Distribution newInstance(File zipFile, FileSystemModule fs) throws DeploymentException {
    try {

      return newInstance(fs.openZipEntryStream(zipFile, DEPLOYMENT_DESCRIPTOR));
    } catch (IOException e) {
      throw new DeploymentException("could not extract entry: " + DEPLOYMENT_DESCRIPTOR + " from " + zipFile.getAbsolutePath(), e);
    }
  }

  public String getDislayInfo() {
    return String.format("[%s-%s]", name, version);
  }
  
  // --------------------------------------------------------------------------
  
  @Override
  public void toJson(JsonStream stream) {
    stream.beginObject()
      .field("name").value(name)
      .field("version").value(version);
    
    stream.field("processConfigs").beginArray();
    for (ProcessConfig pc : processConfigs) {
      stream.beginObject()
        .field("name").value(pc.getName())
        .field("maxInstances").value(pc.getMaxInstances())
        .field("maxKillRetry").value(pc.getMaxKillRetry())
        .field("pollInterval").value(pc.getPollInterval())
        .field("shutdownTimeout").value(pc.getShutdownTimeout())
        .field("statusInterval").value(pc.getStatusInterval())
        .field("deleteOnKill").value(pc.isDeleteOnKill())
        .field("invoke").value(pc.isInvoke());
      
      stream.field("ports");
      stream.strings(Collects.convertAsArray(pc.getPorts(), String.class, new Func<String, Port>() {
        @Override
        public String call(Port port) {
          return port.getName();
        }
      }));
      
      stream.field("profiles");
      stream.strings(pc.getProfiles().toArray(new String[pc.getProfiles().size()]));
      
      stream.field("tags");
      stream.strings(pc.getTagSet().toArray(new String[pc.getTagSet().size()]));
      stream.endObject();
    }
    stream.endArray();
    stream.endObject();
  }
  
  // --------------------------------------------------------------------------
  
  @Override
  public String toString() {  
    return "[ name=" + name + ", version=" + version + ", processes=" + processConfigs.toString() + " ]";
  }

  @Override
  public int hashCode() {
    return name.hashCode() ^ version.hashCode();
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof Distribution) {
      Distribution otherDist = (Distribution) other;
      return name.equals(otherDist.getName()) && version.equals(otherDist.getVersion());
    } else {
      return false;
    }
  }

  @Override
  public int compareTo(Distribution other) {
    int c = name.compareTo(other.getName());
    if (c == 0) {
      c = version.compareTo(other.getVersion());
    }
    return c;
  }

  public Object onCreate() throws ConfigurationException {
    for (ProcessConfig cfg : processConfigs) {
      cfg.init(name, version);
    }
    return this;
  }

  private boolean matchesProcesses(Pattern pattern) {
    for (ProcessConfig pc : processConfigs) {
      if (pc.matches(pattern)) {
        return true;
      }
    }
    return false;
  }

}
