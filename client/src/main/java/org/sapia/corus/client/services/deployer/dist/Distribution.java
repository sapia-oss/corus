package org.sapia.corus.client.services.deployer.dist;

import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.ZipUtils;
import org.sapia.corus.client.exceptions.deployer.DeploymentException;
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
public class Distribution implements java.io.Serializable, ObjectCreationCallback{
  
  static final long serialVersionUID = 1L;

  private static final String DEPLOYMENT_DESCRIPTOR = "META-INF/corus.xml";
  private static final int    CAPACITY   = 4096;
  private String              _name;
  private String              _version;
  private String              _baseDir;
  private String              _commonDir;
  private String              _processesDir;
  private String[]            _tags;
  private List<ProcessConfig> _processConfigs = new ArrayList<ProcessConfig>();

  /**
   * Sets this distribution's name.
   *
   * @param name a name.
   */
  public void setName(String name) {
    _name = name;
  }
  
  /**
   * Sets this instance's tags.
   * @param tagList
   */
  public void setTags(String tagList){
    _tags = tagList.split(",");
    for(int i = 0; i < _tags.length; i++){
      _tags[i] = _tags[i].trim();
    }
  }
  
  /**
   * @return the set of tags held by this instance.
   */
  public Set<String> getTagSet(){
    Set<String> set = new HashSet<String>();
    if(_tags != null){
      for(String t:_tags){
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
    return _name;
  }

  /**
   * Sets this distribution's version.
   *
   * @param version a version.
   */
  public void setVersion(String version) {
    _version = version;
  }

  /**
   * Returns this distribution's version.
   *
   * @return a version as a string.
   */
  public String getVersion() {
    return _version;
  }

  /**
   * Adds a process configuration to this distribution.
   *
   * @param a <code>Process</code> instance, representing a process configuration.
   */
  public void addProcess(ProcessConfig conf) {
    _processConfigs.add(conf);
  }

  /**
   * Returns the process configurations of this distribution.
   *
   * @return the <code>List</code> of <code>Vm</code>
   * instances in this distribution.
   */
  public List<ProcessConfig> getProcesses() {
    return _processConfigs;
  }
  
  /**
   * Tests for the presence of a process configuration within this distribution.
   * 
   * @param name the name of a process configuration to test for.
   * @return <code>true</code> if this instance contains a process configuration
   * with the given name.
   */
  public boolean containsProcess(String name) {
    for(ProcessConfig pc: _processConfigs){
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
   * @return the <code>ProcessConfig</code> that corresponds to the given name,
   * or <code>null</code> if none could be found.
   */  
  public ProcessConfig getProcess(String name) {
    for (ProcessConfig pc: _processConfigs) {
      if (name.matches(pc.getName())) {
        return pc;
      }
    }
    return null;
  }
  
  public List<ProcessConfig> getProcesses(Arg name) {
    List<ProcessConfig> toReturn = new ArrayList<ProcessConfig>();
    
    for (ProcessConfig pc: _processConfigs) {
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
    _baseDir   = baseDir;
    _commonDir = baseDir + File.separator + "common";
    _processesDir = baseDir + File.separator + "processes";
  }

  /**
   * Returns the base directory of this distribution.
   *
   * @return the full path to the directory of this distribution under
   * the Corus server.
   */
  public String getBaseDir() {
    return _baseDir;
  }

  /**
   * Returns the process process directory.
   *
   * @return the full path to the process process directory.
   */
  public String getProcessesDir() {
    return _processesDir;
  }

  /**
   * Returns the common directory (the directory shared by all processes or
   * the directory where the distribution's archive was extracted).
   */
  public String getCommonDir() {
    return _commonDir;
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
   * @param jarName the name of the jar to search the configuration in.
   * configuration.
   *
   * @throws DeploymentException if a problem occurs creating the <code>Distribution</code> object.
   */
  public static Distribution newInstance(String jarName)
                                  throws DeploymentException {
    try {

      return newInstance(ZipUtils.readEntryStream(jarName,
                                                  DEPLOYMENT_DESCRIPTOR,
                                                  CAPACITY, CAPACITY));
    } catch (IOException e) {
      throw new DeploymentException("could not extract entry: " +
                                    DEPLOYMENT_DESCRIPTOR, e);
    }
  }

  public String toString() {
    return "[ name=" + _name + ", version=" + _version + ", processes=" +
           _processConfigs.toString() + " ]";
  }
  
  public int hashCode(){
    return _name.hashCode() ^ _version.hashCode();
  }
  
  public boolean equals(Object other){
    if(other instanceof Distribution){
      Distribution otherDist = (Distribution)other;
      return _name.equals(otherDist.getName()) && _version.equals(otherDist.getVersion());
    }
    else{
      return false;
    }
  }
  
  public Object onCreate() throws ConfigurationException {
    for(ProcessConfig cfg:_processConfigs){
      cfg.init(_name, _version);
    }
    return this;
  }

}
