package org.sapia.corus.client.services.deployer.dist;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.exceptions.deployer.DeploymentException;
import org.sapia.corus.client.services.file.FileSystemModule;
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
public class Distribution implements java.io.Serializable, ObjectCreationCallback, Comparable<Distribution>{
  
  static final long serialVersionUID = 1L;

  private static final String DEPLOYMENT_DESCRIPTOR = "META-INF/corus.xml";
  private String              name;
  private String              version;
  private String              baseDir;
  private String              commonDir;
  private String              processesDir;
  private String[]            tags;
  private List<ProcessConfig> processConfigs = new ArrayList<ProcessConfig>();
  
  public Distribution(){}
  
  public Distribution(String name, String version){
    this.name		 = name;
    this.version = version;
  }

  /**
   * Sets this distribution's name.
   *
   * @param name a name.
   */
  public void setName(String name) {
    this.name = name;
  }
  
  /**
   * Sets this instance's tags.
   * @param tagList
   */
  public void setTags(String tagList){
    tags = tagList.split(",");
    for(int i = 0; i < tags.length; i++){
      tags[i] = tags[i].trim();
    }
  }
  
  /**
   * @return the set of tags held by this instance.
   */
  public Set<String> getTagSet(){
    Set<String> set = new HashSet<String>();
    if(tags != null){
      for(String t:tags){
        set.add(t);
      }
    }
    return set;
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
   * @param version a version.
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
   * Adds a process configuration to this distribution.
   *
   * @param conf a {@link ProcessConfig} instance, representing a process configuration.
   */
  public void addProcess(ProcessConfig conf) {
    processConfigs.add(conf);
  }

  /**
   * Returns the process configurations of this distribution.
   *
   * @return the list of {@link ProcessConfig} instances that this
   * distribution holds.
   */
  public List<ProcessConfig> getProcesses() {
    return processConfigs;
  }
  
  /**
   * Tests for the presence of a process configuration within this distribution.
   * 
   * @param name the name of a process configuration to test for.
   * @return <code>true</code> if this instance contains a process configuration
   * with the given name.
   */
  public boolean containsProcess(String name) {
    for(ProcessConfig pc: processConfigs){
      if(pc.getName().equals(name)){
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the <code>ProcessConfig</code> that corresponds to the given name.
   * 
   * @param name the name of a process configuration to test for.
   * @return the {@link ProcessConfig} that corresponds to the given name,
   * or <code>null</code> if none could be found.
   */  
  public ProcessConfig getProcess(String name) {
    for (ProcessConfig pc: processConfigs) {
      if (name.matches(pc.getName())) {
        return pc;
      }
    }
    return null;
  }
  
  /**
   * @param name an {@link Arg} corresponding to the process name to match.
   * @return the {@link ProcessConfig}s whose name match the given argument.
   */
  public List<ProcessConfig> getProcesses(Arg name) {
    List<ProcessConfig> toReturn = new ArrayList<ProcessConfig>();
    
    for (ProcessConfig pc: processConfigs) {
      if (name.matches(pc.getName())) {
        toReturn.add(pc);
      }
    }
    return toReturn;
  }
  
  /**
   * Sets this distribution base directory.
   *
   * @param baseDir this distribution's base dir. as a string.
   */
  public void setBaseDir(String baseDir) {
    this.baseDir = baseDir;
    commonDir 	 = baseDir + File.separator + "common";
    processesDir = baseDir + File.separator + "processes";
  }

  /**
   * Returns the base directory of this distribution.
   *
   * @return the full path to the directory of this distribution under
   * the Corus server.
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
   * Returns the common directory (the directory shared by all processes or
   * the directory where the distribution's archive was extracted).
   */
  public String getCommonDir() {
    return commonDir;
  }
  
  /**
   * Tests if this distribution's name and version match the given corresponding
   * arguments.
   * 
   * @param name an {@link Arg} to test against this instance's name.
   * @param version an {@link Arg} to test against this instance's version.
   * @return <code>true</code> if this instance matches the given name and version.
   */
  public boolean matches(Arg name, Arg version){
    if(name.matches(this.name)){
      return version.matches(this.version);
    }
    return false;
  }

  /**
   * Returns an instance of this class built from the <code>corus.xml</code>
   * configuration whose content is passed as the given <code>InputStream</code>.
   *
   * @param is an <code>InputStream</code> corresponding to a <code>corus.xml</code>
   * configuration.
   *
   * @throws DeploymentException if a problem occurs creating the <code>Distribution</code> object.
   */
  public static Distribution newInstance(InputStream is)
                                  throws DeploymentException {
    ReflectionFactory fac  = new DeployerObjectFactory();
    Dom4jProcessor proc = new Dom4jProcessor(fac);

    try {
      return ((Distribution) proc.process(is));
    } catch (ProcessingException e) {
      throw new DeploymentException(e.getMessage(), e);
    }
  }

  /**
   * Returns an instance of this class built from the <code>corus.xml</code>
   * configuration whose file is in the <code>META-INF</code> directory of
   * the archive whose name is passed as a parameter.
   *
   * @param zipFile the {@link File} corresponding to the zip file to get the configuration
   * from.
   * @param the {@link FileSystemModule} abstracting the file system to read from.
   *
   * @throws DeploymentException if a problem occurs creating the <code>Distribution</code> object.
   */
  public static Distribution newInstance(File zipFile, FileSystemModule fs)
                                  throws DeploymentException {
    try {

      return newInstance(fs.openZipEntryStream(zipFile, DEPLOYMENT_DESCRIPTOR));
    } catch (IOException e) {
      throw new DeploymentException("could not extract entry: " +
                                    DEPLOYMENT_DESCRIPTOR, e);
    }
  }
  
  public String getDislayInfo(){
    return String.format("[%s-%s]", name, version);
  }

  public String toString() {
    return "[ name=" + name + ", version=" + version + ", processes=" +
           processConfigs.toString() + " ]";
  }
  
  public int hashCode(){
    return name.hashCode() ^ version.hashCode();
  }
  
  public boolean equals(Object other){
    if(other instanceof Distribution){
      Distribution otherDist = (Distribution)other;
      return name.equals(otherDist.getName()) && version.equals(otherDist.getVersion());
    }
    else{
      return false;
    }
  }
  
  @Override
  public int compareTo(Distribution other) {
    int c = name.compareTo(other.getName());
    if(c == 0){
      c = version.compareTo(other.getVersion());
    }
    return c;
  }
  
  public Object onCreate() throws ConfigurationException {
    for(ProcessConfig cfg:processConfigs){
      cfg.init(name, version);
    }
    return this;
  }

}
