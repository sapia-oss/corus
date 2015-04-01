package org.sapia.corus.cloud.topology;

import java.util.HashSet;
import java.util.Set;

/**
 * Corresponds to the <code>cluster-template</code> element.
 * 
 * @author yduchesne
 *
 */
public class ClusterTemplate {

  private String       name;
  private int          instances = -1;
  private Set<Machine> machines  = new HashSet<>();
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public void setInstances(int instances) {
    this.instances = instances;
  }
  
  public int getInstances() {
    return instances;
  }
  
  public void addMachine(Machine m) {
    if (!machines.add(m)) {
      throw new IllegalArgumentException(String.format("Duplicate <machine> %s in <cluster> (or <cluster-template>) %s", m.getName(), name));
    }
  }
  
  public Set<Machine> getMachines() {
    return machines;
  }

  public void copyFrom(ClusterTemplate other) {
    if (instances < 0) {
      instances = other.instances;
    }
    machines.addAll(other.getMachines());
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ClusterTemplate) {
      ClusterTemplate other = (ClusterTemplate) obj;
      if (name == null || other.name == null) {
        return false;
      }
      return name.equals(other.name);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    if (name == null) {
      return super.hashCode();
    } else {
      return name.hashCode();
    }
  }
}
