package org.sapia.corus.cloud.topology;

import java.util.HashSet;
import java.util.Set;

/**
 * Corresponds to the <code>serverProperties</code> and <code>processProperties</code>
 * elements.
 * 
 * @author yduchesne
 *
 */
public class PropertyCollection {
  
  private Set<CorusProperty> properties = new HashSet<>();
  
  public void addProperty(CorusProperty property) {
    this.properties.add(property);
  }
  
  public void addProperty(String name, String value) {
    CorusProperty p = new CorusProperty();
    p.setName(name);
    p.setValue(value);
    addProperty(p);
  }
  
  public Set<CorusProperty> getProperties() {
    return properties;
  }
  
  public void copyFrom(PropertyCollection other) {
    properties.addAll(other.properties);
  }
  
}