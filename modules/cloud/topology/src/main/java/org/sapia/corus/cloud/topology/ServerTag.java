package org.sapia.corus.cloud.topology;

/**
 * Corresponds to the <code>serverTag</code> element.
 * 
 * @author yduchesne
 *
 */
public class ServerTag implements XmlStreamable, Validateable {

  private String value;

  public ServerTag() {
  }
  
  public ServerTag(String value) {
    this.value = value;
  }
  
  public void setValue(String value) {
    this.value = value;
  }
  
  public String getValue() {
    return value;
  }
  
  public void setText(String value) {
    this.value = value;
  }
  
  public static ServerTag of(String value) {
    return new ServerTag(value);
  }
  
  // --------------------------------------------------------------------------
  // Validateable
  
  @Override
  public void validate() throws IllegalArgumentException {
    if (value == null) {
      throw new IllegalArgumentException("text not specified for <serverTag> element");
    }    
  }
  
  // --------------------------------------------------------------------------
  // XmlStreameable
  
  @Override
  public void output(XmlStream stream) {
    stream.beginElement("serverTag");
    stream.cdata(value);
    stream.endElement("serverTag");
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ServerTag) {
      ServerTag other = (ServerTag) obj;
      if (value == null || other.value == null) {
        return false;
      }
      return value.equals(other.value);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    if (value == null) {
      return super.hashCode();
    } else {
      return value.hashCode();
    }
  }
  
}
