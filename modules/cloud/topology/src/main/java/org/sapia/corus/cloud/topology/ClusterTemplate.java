package org.sapia.corus.cloud.topology;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Corresponds to the <code>cluster-template</code> element.
 * 
 * @author yduchesne
 *
 */
public class ClusterTemplate extends ParamContainer {

  private String       name;
  private int          instances = -1;
  private Set<Machine> machines  = new HashSet<>();
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public String getAlphaNumericName() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (Character.isAlphabetic(c) || Character.isDigit(c)) {
        sb.append(c);
      }
    }
    return sb.toString();
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
  
  public Collection<Machine> getSortedMachines() {
    List<Machine> sortedMachines = new ArrayList<>(machines);
    Collections.sort(sortedMachines, new Comparator<Machine>() {
      @Override
      public int compare(Machine o1, Machine o2) {
        int c = 0;
        if (o1.isSeedNode() && !o2.isSeedNode()) {
          c = -1;
        } else if (!o1.isSeedNode() && o2.isSeedNode()) {
          c = 1;
        }
        return c;
      }
    });
    return sortedMachines;
  }

  public void copyFrom(ClusterTemplate other) {
    if (instances < 0) {
      instances = other.instances;
    }
    machines.addAll(other.getMachines());
    addParams(other.getParams());
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
