package org.sapia.corus.admin.services.processor;

import java.io.Serializable;


/**
 * Keeps information pertaining to the distribution a process "comes" from.
 *
 * @author Yanick Duchesne
 */
public class DistributionInfo implements Serializable {
  
  static final long serialVersionUID = 1L;

  private String _name;
  private String _version;
  private String _profile;
  private String _processName;

  /**
   * Creates a new instance of this class with the given params.
   *
   * @param name the distribution's name.
   * @param version the distribution's version.
   * @param profile the distribution's profile.
   * @param vmName the distribution's process configuration name.
   */
  public DistributionInfo(String name, String version, String profile,
                          String processName) {
    _name    = name;
    _version = version;
    _profile = profile;
    _processName  = processName;
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
   * Returns the name of the distribution to which this instance
   * corresponds.
   *
   * @return a distribution name.
   */
  public String getName() {
    return _name;
  }

  /**
   * Returns the name of the process configuration to which this instance
   * corresponds.
   *
   * @return a process name.
   */
  public String getProcessName() {
    return _processName;
  }

  /**
   * Returns the version of the distribution to which this instance
   * corresponds.
   *
   * @return a version as a string.
   */
  public String getVersion() {
    return _version;
  }

  /**
   * Return this instance's string representation.
   *
   * @return this as a string.
   */
  public String toString() {
    return "[ dist=" + _name + ", version=" + _version + ", profile=" +
           _profile + ", process name=" + _processName + " ]";
  }
}
