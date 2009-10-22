package org.sapia.corus.processor;

import org.sapia.corus.admin.services.deployer.dist.Distribution;
import org.sapia.corus.admin.services.deployer.dist.ProcessConfig;

public class ProcessRef {

  private Distribution dist;
  private ProcessConfig processConf;
  private String profile;
  private int instanceCount = 1;
  private boolean isRoot;

  public ProcessRef(Distribution dist, ProcessConfig conf, String profile) {
    this.dist = dist;
    this.processConf = conf;
    this.profile = profile;
  }

  public ProcessRef setRoot(boolean isRoot) {
    this.isRoot = isRoot;
    return this;
  }
  
  public boolean isRoot() {
    return isRoot;
  }
  
  public int getInstanceCount() {
    return instanceCount;
  }
  
  public ProcessRef setInstanceCount(int instanceCount){
    this.instanceCount = instanceCount;
    return this;
  }
  
  public void setInstances(int count){
    this.instanceCount = count;
  }

  public String getProfile() {
    return profile;
  }

  public Distribution getDist() {
    return dist;
  }

  public ProcessConfig getProcessConfig() {
    return processConf;
  }

  public int hashCode() {
    return processConf.hashCode() ^ dist.hashCode();
  }

  public boolean equals(Object other) {
    if (other instanceof ProcessRef) {
      ProcessRef otherRef = (ProcessRef) other;
      return dist.equals(otherRef.dist)
          && processConf.equals(otherRef.processConf);
    } else {
      return false;
    }
  }
  
  public ProcessRef getCopy(){
    ProcessRef c = new ProcessRef(dist, processConf, profile).setRoot(isRoot).setInstanceCount(instanceCount);
    return c;
  }
  
  @Override
  public String toString(){
    return new StringBuilder("[")
    .append("dist=").append(dist.getName()).append(", ")
    .append("version=").append(dist.getVersion()).append(", ")
    .append("process=").append(processConf.getName())
    .append("]")
    .toString();
  }
   
}
