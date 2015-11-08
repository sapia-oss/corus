package org.sapia.corus.cloud.topology;

/**
 * Corresponds to the <code>property</code> elements.
 * 
 * @author yduchesne
 *
 */
public class Property implements XmlStreamable, Validateable {

  private String name, value;
  
  public Property() {
  }
  
  public Property(String name, String value) {
    this.name  = name;
    this.value = value;
  }
  
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
  
  public static Property of(String name, String value) {
    return new Property(name, value);
  }
  
  // --------------------------------------------------------------------------
  // Validateable
  
  @Override
  public void validate() throws IllegalArgumentException {
    if (name == null) {
      throw new IllegalArgumentException("'name' attribute not specified on <property> element");
    }
    
    if (value == null) {
      throw new IllegalArgumentException("'value' attribute not specified on <property> " + name);
    }
  }
   
  // --------------------------------------------------------------------------
  // XmlStreamable
  
  @Override
  public void output(XmlStream stream) {
    stream.beginElement("property");
    stream.attribute("name", name);
    stream.attribute("value", value);
    stream.endElement("property");
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public int hashCode() {
    return name == null ? super.hashCode() : name.hashCode();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Property) {
      Property p = (Property) obj;
      if (name == null  || p.name == null) {
        return false;
      }
      return name.equals(p.name);
    }
    return false;
  }
  
}