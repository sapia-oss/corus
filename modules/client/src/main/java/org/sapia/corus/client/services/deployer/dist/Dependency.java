package org.sapia.corus.client.services.deployer.dist;

import static org.sapia.corus.client.services.deployer.dist.ConfigAssertions.attributeNotNullOrEmpty;
import static org.sapia.corus.client.services.deployer.dist.ConfigAssertions.optionalAttributeNotNullOrEmpty;

import java.io.Serializable;

import org.sapia.corus.client.common.OptionalValue;
import org.sapia.util.xml.confix.ConfigurationException;
import org.sapia.util.xml.confix.ObjectCreationCallback;

/**
 * This class models a process dependency: such a dependency is a process (the
 * "dependee") that another process depends on, and should therefore be executed
 * prior to the dependent process.
 * 
 * @author yduchesne
 * 
 */
public class Dependency implements Serializable, ObjectCreationCallback {

  static final long serialVersionUID = 1L;

  private OptionalValue<String> dist    = OptionalValue.none();
  private OptionalValue<String> version = OptionalValue.none();
  private String                process;
  private OptionalValue<String> profile = OptionalValue.none();

  /**
   * @return the name of the distribution to which the "dependee" process
   *         belongs, or <code>null</code> if it belongs to the same
   *         distribution as the dependent process.
   */
  public OptionalValue<String> getDist() {
    return dist;
  }

  public void setDistribution(String dist) {
    setDist(dist);
  }

  public void setDist(String dist) {
    this.dist = OptionalValue.of(dist);
  }

  /**
   * @return the version of the distribution to which the "dependee" process
   *         belongs, or <code>null</code> if it belongs to the same
   *         distribution as the dependent process.
   */
  public OptionalValue<String> getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = OptionalValue.of(version);
  }

  /**
   * @return the profile of of the "dependee" process.
   */
  public OptionalValue<String> getProfile() {
    return profile;
  }

  public void setProfile(String profile) {
    this.profile = OptionalValue.of(profile);
  }

  /**
   * @return the name of "dependee" process.
   */
  public String getProcess() {
    return process;
  }

  public void setProcess(String process) {
    this.process = process;
  }

  // --------------------------------------------------------------------------
  // ObjectCreationCallback
  
  @Override
  public Object onCreate() throws ConfigurationException {
    attributeNotNullOrEmpty("dependency", "process", process);
    optionalAttributeNotNullOrEmpty("dependency", "dist", getDist());
    optionalAttributeNotNullOrEmpty("dependency", "version", getVersion());
    optionalAttributeNotNullOrEmpty("dependency", "profile", getProfile());
    return this;
  }
  
  // --------------------------------------------------------------------------
  // Object overriddes

  @Override
  public int hashCode() {
    return dist.hashCode() ^ (version == null ? 0 : version.hashCode()) ^ (process == null ? 0 : process.hashCode());
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof Dependency) {
      Dependency otherDep = (Dependency) other;
      return otherDep.getDist().equals(dist) && otherDep.getVersion().equals(version) && otherDep.getProcess().equals(process);
    } 
    return false;
  }

  @Override
  public String toString() {
    return new StringBuilder("[").append("dist=").append(dist).append(", ").append("version=").append(version).append(", ").append("profile=")
        .append(profile).append("]").toString();
  }

}
