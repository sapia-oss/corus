package org.sapia.corus.client.services.repository;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.util.Strings;

/**
 * Holds a distribution's name and version.
 * 
 * @author yduchesne
 *
 */
public class RepoDistribution implements Externalizable {
  
  private String name;
  private String version;

  /** DO NOT USE: meant for externalization only. */
  public RepoDistribution() {
  }
  
  /**
   * @param name the distribution to which this instance corresponds.
   * @param version the version to which this instance corresponds.
   */
  public RepoDistribution(String name, String version) {
    this.name    = name;
    this.version = version;
  }
  
  /**
   * @return this instance's name.
   */
  public String getName() {
    return name;
  }
  
  /**
   * @return this instance's version.
   */
  public String getVersion() {
    return version;
  }
  
  // --------------------------------------------------------------------------
  // Externalization
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    name    = in.readUTF();
    version = in.readUTF();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(name);
    out.writeUTF(version);
  }
  
  // --------------------------------------------------------------------------
  // Object overriddes
  
  @Override
  public int hashCode() {
    return name.hashCode() * 31 + version.hashCode() * 31;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof RepoDistribution) {
      RepoDistribution other = (RepoDistribution) obj;
      return name.equals(other.name) && version.equals(other.version);
    }
    return false;
  }
  
  @Override
  public String toString() {
    return Strings.toStringFor(this, "name", name, "version", version);
  }

}
