package org.sapia.corus.cloud.topology;

/**
 * Corresponds to the <code>param</code> element.
 * 
 * @author yduchesne
 *
 */
public class Param implements XmlStreamable, Validateable {

  private String name, value;
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public void setValue(String value) {
    this.value = value;
  }
  
  public String getValue() {
    return value;
  }
  
  // --------------------------------------------------------------------------
  // Validateable
  
  @Override
  public void validate() throws IllegalArgumentException {
    if (name == null) {
      throw new IllegalArgumentException("attribute 'name' not specified on <param> element");
    }
    
    if (value == null) {
      throw new IllegalArgumentException("attribute 'value' not specified for <param> element: " + name);
    }
  }
  
  // --------------------------------------------------------------------------
  // XmlStreameable
  
  @Override
  public void output(XmlStream stream) {
    stream.beginElement("param");
    stream.attribute("name", name);
    stream.attribute("value", value);
    stream.endElement("param");
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public int hashCode() {
    return name == null ? super.hashCode() : name.hashCode();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Param) {
      Param p = (Param) obj;
      if (name == null || p.name == null) {
        return false;
      }
      return name.equals(p.name);
    }
    return false;
  }
  
}
