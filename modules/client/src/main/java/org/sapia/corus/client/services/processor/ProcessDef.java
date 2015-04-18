package org.sapia.corus.client.services.processor;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

/**
 * A {@link ProcessDef} is held within an {@link ExecConfig}.
 * 
 * @see ExecConfig#getProcesses()
 * 
 * @author yduchesne
 * 
 */
public class ProcessDef implements Externalizable {

  static final long serialVersionUID = 1L;

  private String dist, process, version, profile;
  private int instances;
  
  public String getDist() {
    return dist;
  }

  public void setDist(String dist) {
    this.dist = dist;
  }

  public void setDistribution(String dist) {
    setDist(dist);
  }

  public void setName(String process) {
    this.process = process;
  }

  public String getName() {
    return process;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getProfile() {
    return profile;
  }

  public void setProfile(String profile) {
    this.profile = profile;
  }

  public void setInstances(int instances) {
    this.instances = instances;
  }

  public int getInstances() {
    if (instances == 0) {
      instances = 1;
    }
    return instances;
  }
  
  // --------------------------------------------------------------------------
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    dist      = in.readUTF();
    process   = in.readUTF();
    version   = in.readUTF();
    profile   = in.readUTF();
    instances = in.readInt();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(dist);
    out.writeUTF(process);
    out.writeUTF(version);
    out.writeUTF(profile);
    out.writeInt(instances);
  }

  // --------------------------------------------------------------------------
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ProcessDef) {
      ProcessDef other = (ProcessDef) obj;
      return other.getDist().equals(dist) && other.getVersion().equals(version) && other.getName().equals(process)
          && other.getProfile().equals(profile);
    }
    return false;

  }

  @Override
  public int hashCode() {
    return new StringBuilder().append(dist).append(version).append(process).append(profile).toString().hashCode();
  }
  
  // --------------------------------------------------------------------------

  @Override
  public String toString() {
    return new StringBuilder("[").append("dist=").append(dist).append(", ").append("version=").append(version).append(", ").append("name=")
        .append(process).append(", ").append("profile=").append(profile).append("instances=").append(instances).append("]").toString();
  }

}
