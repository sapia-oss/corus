package org.sapia.corus.deployer.config;

import org.sapia.corus.admin.CommandArg;
import org.sapia.corus.deployer.DeploymentException;
import org.sapia.corus.util.ZipUtils;

import org.sapia.util.xml.ProcessingException;
import org.sapia.util.xml.confix.Dom4jProcessor;
import org.sapia.util.xml.confix.ReflectionFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;


/**
 * This class corresponds to the <code>distribution</code> element in the
 * corus.xml file.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Distribution implements java.io.Serializable {
  private static final String DEPLOYMENT_DESCRIPTOR = "META-INF/corus.xml";
  private static final int    CAPACITY   = 4096;
  private String              _name;
  private String              _version;
  private String              _baseDir;
  private String              _commonDir;
  private String              _vmsDir;
  private List                _vms       = new ArrayList();

  /**
   * Sets this distribution's name.
   *
   * @param name a name.
   */
  public void setName(String name) {
    _name = name;
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
  public void addProcess(ProcessConfig vm) {
    _vms.add(vm);
  }

  /**
   * Returns the process configurations of this distribution.
   *
   * @return the <code>List</code> of <code>Vm</code>
   * instances in this distribution.
   */
  public List getProcesses() {
    return _vms;
  }
  
  /**
   * Tests for the presence of a process configuration within this distribution.
   * 
   * @param name the name of a process configuration to test for.
   * @return <code>true</code> if this instance contains a process configuration
   * with the given name.
   */
  public boolean containsProcess(String name) {
    ProcessConfig vm;

    for (int i = 0; i < _vms.size(); i++) {
      vm = (ProcessConfig) _vms.get(i);

      if (vm.getName().equals(name)) {
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
    ProcessConfig vm;

    for (int i = 0; i < _vms.size(); i++) {
      vm = (ProcessConfig) _vms.get(i);

      if (vm.getName().equals(name)) {
        return vm;
      }
    }

    return null;
  }
  
  public List getProcesses(CommandArg name) {
    List toReturn = new ArrayList();
    
    ProcessConfig vm;

    for (int i = 0; i < _vms.size(); i++) {
      vm = (ProcessConfig) _vms.get(i);

      if (name.matches(vm.getName())) {
        toReturn.add(vm);
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
    _vmsDir    = baseDir + File.separator + "processes";
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
    return _vmsDir;
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
      e.printStackTrace();
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
    return "[ name=" + _name + ", version=" + _version + ", vm configs=" +
           _vms.toString() + " ]";
  }

  public static void main(String[] args) throws Throwable {
    Distribution d = Distribution.newInstance("dist/dummyDist2.jar");
    System.out.println(d.getName());
  }
}
