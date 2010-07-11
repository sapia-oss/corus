package org.sapia.corus.client.services.deployer.dist;

import java.io.Serializable;

/**
 * This class models a process dependency: such a dependency is a process (the "dependee") 
 * that another process depends on, and should therefore be executed prior to the dependent 
 * process.
 * 
 * @author yduchesne
 *
 */
public class Dependency implements Serializable{
  
  static final long serialVersionUID = 1L;

  private String dist, version, process, profile;

  /**
   * @return the name of the distribution to which the "dependee" process belongs, or
   * <code>null</code> if it belongs to the same distribution as the dependent process.
   */
  public String getDist() {
    return dist;
  }
  
  public void setDistribution(String dist){
    setDist(dist);
  }

  public void setDist(String dist) {
    this.dist = dist;
  }

  /**
   * @return the version of the distribution to which the "dependee" process belongs,
   * or <code>null</code> if it belongs to the same distribution as the dependent process.
   */
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * @return the profile of of the "dependee" process.
   */
  public String getProfile() {
    return profile;
  }

  public void setProfile(String profile) {
    this.profile = profile;
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
  
  public int hashCode(){
    return 
      dist.hashCode() ^ 
      (version == null ? 0 : version.hashCode()) ^
      (process == null ? 0 : process.hashCode());
  }
  
  public boolean equals(Object other){
    if(other instanceof Dependency){
      Dependency otherDep = (Dependency)other;
      return otherDep.getDist().equals(dist) && 
             otherDep.getVersion().equals(version) &&
             otherDep.getProcess().equals(process);
    }
    else{
      return false;
    }
  }
  
  public String toString(){
    return new StringBuilder("[")
      .append("dist=").append(dist).append(", ")
      .append("version=").append(version).append(", ")
      .append("profile=").append(profile).append("]").toString();
  }
  
}
