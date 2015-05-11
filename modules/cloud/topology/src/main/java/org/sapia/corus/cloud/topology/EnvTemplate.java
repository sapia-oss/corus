package org.sapia.corus.cloud.topology;

import java.util.HashSet;
import java.util.Set;

/**
 * Corresponds to the <code>env-template</code> element.
 * 
 * @author yduchesne
 *
 */
public class EnvTemplate extends ParamContainer {

  private String       name;
  private Set<Region>  regions  = new HashSet<>();
  private Set<Cluster> clusters = new HashSet<>();
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public void addRegion(Region r) {
    if (!regions.add(r)) {
      throw new IllegalArgumentException(String.format("Duplicate <region> element %s under <env> (or <env-template>) element %s: ", r.getName(), name));
    }
  }
  
  public Set<Region> getRegions() {
    return regions;
  }
  
  public void addCluster(Cluster c) {
    if (!clusters.add(c)) {
      throw new IllegalArgumentException(String.format("Duplicate <cluster> element %s under <env> (or <env-template>) element %s: ", c.getName(), name));
    }
  }
  
  public Set<Cluster> getClusters() {
    return clusters;
  }
  
  public void copyFrom(EnvTemplate other) {
    regions.addAll(other.regions);
    clusters.addAll(other.clusters);
    addParams(other.getParams());
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof EnvTemplate) {
      EnvTemplate other = (EnvTemplate) obj;
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
