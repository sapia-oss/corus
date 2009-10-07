package org.sapia.corus.admin.services.deployer.dist;

import java.io.Serializable;

public class Dependency implements Serializable{
  
  static final long serialVersionUID = 1L;

  private String dist, version, process, profile;

  public String getDist() {
    return dist;
  }
  
  public void setDistribution(String dist){
    setDist(dist);
  }

  public void setDist(String dist) {
    this.dist = dist;
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
  
  public String getProcess() {
    return process;
  }
  
  public void setProcess(String process) {
    this.process = process;
  }
  
  public String toString(){
    return new StringBuilder("[")
      .append("dist=").append(dist).append(", ")
      .append("version=").append(version).append(", ")
      .append("profile=").append(profile).append("]").toString();
  }
  
}
