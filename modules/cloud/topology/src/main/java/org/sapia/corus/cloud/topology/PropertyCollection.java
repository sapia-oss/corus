package org.sapia.corus.cloud.topology;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Corresponds to the <code>serverProperties</code> and <code>processProperties</code>
 * elements.
 * 
 * @author yduchesne
 *
 */
public class PropertyCollection implements Iterable<Property> {
  
  private Set<Property> properties = new HashSet<>();
  
  public PropertyCollection addProperty(Property property) {
    this.properties.add(property);
    return this;
  }
  
  public PropertyCollection addProperty(String name, String value) {
    Property p = new Property();
    p.setName(name);
    p.setValue(value);
    properties.add(p);
    return this;
  }
  
  public Set<Property> getProperties() {
    return properties;
  }
  
  public void copyFrom(PropertyCollection other) {
    properties.addAll(other.properties);
  }
  
  @Override
  public Iterator<Property> iterator() {
    return properties.iterator();
  }
  
  public int size() {
    return properties.size();
  }
  
}