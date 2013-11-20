package org.sapia.corus.processor;

import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.ProcessCriteria;

/**
 * Models a reference to a {@link ProcessConfig} and its corresponding
 * {@link Distribution}. This class is used as part of resolving process
 * dependencies.
 * 
 * @author yduchesne
 * 
 */
public class ProcessRef {

  private Distribution dist;
  private ProcessConfig processConf;
  private String profile;
  private int instanceCount = 1;
  private boolean isRoot;

  /**
   * @param dist
   *          a {@link Distribution}.
   * @param conf
   *          a {@link ProcessConfig}.
   * @param profile
   *          a profile.
   */
  public ProcessRef(Distribution dist, ProcessConfig conf, String profile) {
    this.dist = dist;
    this.processConf = conf;
    this.profile = profile;
  }

  /**
   * @param isRoot
   *          <code>true</code> if this instance corresponds to the root of the
   *          process dependency graph.
   * 
   * @return this instance.
   */
  public ProcessRef setRoot(boolean isRoot) {
    this.isRoot = isRoot;
    return this;
  }

  /**
   * @return <code>true</code> if this instance corresponds to the root of the
   *         dependency graph.
   */
  public boolean isRoot() {
    return isRoot;
  }

  /**
   * @return this process ref's instance count.
   */
  public int getInstanceCount() {
    return instanceCount;
  }

  public ProcessRef setInstanceCount(int instanceCount) {
    this.instanceCount = instanceCount;
    return this;
  }

  public void setInstances(int count) {
    this.instanceCount = count;
  }

  /**
   * @return this instance's profile.
   */
  public String getProfile() {
    return profile;
  }

  /**
   * @return this instance's {@link Distribution}.
   */
  public Distribution getDist() {
    return dist;
  }

  /**
   * @return this instance's {@link ProcessConfig}.
   */
  public ProcessConfig getProcessConfig() {
    return processConf;
  }

  /**
   * @return the {@link ProcessCriteria} corresponding to this instance.
   */
  public ProcessCriteria getCriteria() {
    return ProcessCriteria.builder().distribution(dist.getName()).version(dist.getVersion()).name(processConf.getName()).profile(profile).build();
  }

  public int hashCode() {
    return processConf.hashCode() ^ dist.hashCode();
  }

  public boolean equals(Object other) {
    if (other instanceof ProcessRef) {
      ProcessRef otherRef = (ProcessRef) other;
      return dist.equals(otherRef.dist) && processConf.equals(otherRef.processConf);
    } else {
      return false;
    }
  }

  public ProcessRef getCopy() {
    ProcessRef c = new ProcessRef(dist, processConf, profile).setRoot(isRoot).setInstanceCount(instanceCount);
    return c;
  }

  @Override
  public String toString() {
    return new StringBuilder("[").append("dist=").append(dist.getName()).append(", ").append("version=").append(dist.getVersion()).append(", ")
        .append("process=").append(processConf.getName()).append(", ").append("profile=").append(getProfile()).append("]").toString();
  }

}
