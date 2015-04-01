package org.sapia.corus.cloud.topology;

/**
 * Corresponds to the <code>zone</code> element.
 * 
 * @author yduchesne
 *
 */
public class Zone implements XmlStreamable, Validateable {

  private String name;
  
  public Zone() {
  }
  
  public Zone(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public void setText(String zoneName) {
    this.name = zoneName.trim();
  }
  
  public void setName(String zoneName) {
    this.name = zoneName.trim();
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
    stream.cdata(name);
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
