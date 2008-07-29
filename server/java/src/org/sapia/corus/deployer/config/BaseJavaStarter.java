package org.sapia.corus.deployer.config;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;


/**
 * This helper class can be inherited from to implement <code>Starter</code>s that
 * launch Java processes.
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public abstract class BaseJavaStarter implements Starter, Serializable {
  protected String _javaHome   = System.getProperty("java.home");
  protected String _javaCmd    = "java";
  protected String _vmType; 
  protected String _profile;
  protected String _corusHome = System.getProperty("corus.home");
  protected List   _vmProps    = new ArrayList();
  protected List   _options    = new ArrayList();
  protected List   _xoptions   = new ArrayList();

  /**
   * Sets the Corus home.
   *
   * @param home the Corus home.
   */
  public void setCorusHome(String home) {
    _corusHome = home;
  }

  /**
   * Sets this instance's profile.
   *
   * @param profile a profile name.
   */
  public void setProfile(String profile) {
    _profile = profile;
  }

  /**
   * Returns this instance's profile.
   *
   * @return a profile name.
   */
  public String getProfile() {
    return _profile;
  }

  /**
   * Adds the given property to this instance.
   *
   * @param prop a <code>Property</code> instance.
   */
  public void addProperty(Property prop) {
    _vmProps.add(prop);
  }

  /**
   * Adds the given VM option to this instance.
   *
   * @param opt an <code>Option</code> instance.
   */
  public void addOption(Option opt) {
    _options.add(opt);
  }

  /**
   * Adds the given "X" option to this instance.
   *
   * @param opt a <code>XOption</code> instance.
   */
  public void addXoption(XOption opt) {
    _xoptions.add(opt);
  }

  /**
   * Sets this instance's JDK home directory.
   *
   * @param home the full path to a JDK installation directory
   */
  public void setJavaHome(String home) {
    _javaHome = home;
  }

  /**
   * Sets the name of the 'java' executable.
   *
   * @param cmdName the name of the 'java' executable
   */
  public void setJavaCmd(String cmdName) {
    _javaCmd = cmdName;
  }
  
  public void setVmType(String aType) {
    _vmType = aType;
  }
}
