package org.sapia.corus.cloud.topology;

import java.util.HashSet;
import java.util.Set;

/**
 * Corresponds to the <code>region-template</code> element.
 * 
 * @author yduchesne
 *
 */
public class RegionTemplate {

  private String    name;
  private Set<Zone> zones = new HashSet<>();
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public void addZone(Zone z) {
    if (!zones.add(z)) {
      throw new IllegalArgumentException(String.format("Duplicate <zone> element %s under <region> (or <region-template>) element: %s", z.getName(), name));
    }    
  }
  
  public Set<Zone> getZones() {
    return zones;
  }
  
  public void copyFrom(RegionTemplate other) {
    zones.addAll(other.getZones());
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof RegionTemplate) {
      RegionTemplate other = (RegionTemplate) obj;
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
