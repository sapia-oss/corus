package org.sapia.corus.util;

import java.io.Serializable;


/**
 * Implements the {@link Property} interface: encapsulates a string
 * as a property value.
 * 
 * @author yduchesne
 *
 */
public class StringProperty implements Property, Serializable{
  
  static final long serialVersionUID = 1L;
  
  private String value;
  
  public StringProperty(String value) {
    this.value = value;
  }
  
  public String getValue() {
    return value;
  }

  public int getIntValue() {
    return Integer.parseInt(value);
  }
  
  public long getLongValue() {
    return Long.parseLong(value);
  }
  
  public boolean getBooleanValue() {
    return Boolean.parseBoolean(value);
  }
  
  @Override
  public String toString() {
    return getValue();
  }
}
