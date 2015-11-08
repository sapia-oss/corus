package org.sapia.corus.cloud.topology;

/**
 * Corresponds the <code>subnet</code> element.
 * 
 * @author yduchesne
 *
 */
public class Subnet implements XmlStreamable, Validateable {

  private String id;
  
  public Subnet() {
  }
  
  public Subnet(String id) {
    this.id = id;
  }

  public void setId(String id) {
    this.id = id;
  }
  
  public void setText(String id) {
    setId(id);
  }
  
  public String getId() {
    return id;
  }
  
  public static Subnet of(String id) {
    return new Subnet(id);
  }
  
  // --------------------------------------------------------------------------
  // Validateable
  
  @Override
  public void validate() throws IllegalArgumentException {
    if (id == null) {
      throw new IllegalArgumentException("id is empty or not specified for <subnet> element");
    }
  }
  
  // --------------------------------------------------------------------------
  // XmlStreameable
  
  @Override
  public void output(XmlStream stream) {
    stream.beginElement("subnet");
    stream.cdata(id);
    stream.endElement("subnet");
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Subnet) {
      Subnet other = (Subnet) obj;
      if (id == null || other.id == null) {
        return false;
      }
      return id.equals(other.id);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    if (id == null) {
      return super.hashCode();
    } else {
      return id.hashCode();
    }
  }
  
}
