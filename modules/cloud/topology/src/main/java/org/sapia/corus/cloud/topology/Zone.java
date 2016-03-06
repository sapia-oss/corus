package org.sapia.corus.cloud.topology;

import java.util.HashSet;
import java.util.Set;

/**
 * Corresponds to the <code>zone</code> element.
 * 
 * @author yduchesne
 *
 */
public class Zone implements XmlStreamable, Validateable {

  private String name;
  
  private Set<Subnet> subnets = new HashSet<>();
 
  private Region region;
  
  public Zone() {
  }
  
  public Zone(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String zoneName) {
    this.name = zoneName.trim();
  }
  
  public void addSubnet(Subnet subnet) {
    subnets.add(subnet);
  }
  
  public void addSubnets(Set<Subnet> subnets) {
    this.subnets.addAll(subnets);
  }
  
  public Set<Subnet> getSubnets() {
    return subnets;
  }
  
  void setRegion(Region region) {
    this.region = region;
  }
  
  public Region getRegion() {
    return region;
  }
  
  public static Zone of(String name) {
    return new Zone(name);
  }
  
  // --------------------------------------------------------------------------
  // Validateable
  
  @Override
  public void validate() throws IllegalArgumentException {
    if (name == null || name.length() == 0) {
      throw new IllegalArgumentException("text is empty or not specified for <zone> element");
    }
  }
  
  // --------------------------------------------------------------------------
  // XmlStreameable
  
  @Override
  public void output(XmlStream stream) {
    stream.beginElement("zone");
    stream.attribute("name", name);
    for (Subnet n : subnets) {
      n.output(stream);
    }
    stream.endElement("zone");
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Zone) {
      Zone other = (Zone) obj;
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
