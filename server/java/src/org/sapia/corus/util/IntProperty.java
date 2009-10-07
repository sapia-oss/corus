package org.sapia.corus.util;

import java.io.Serializable;

/**
 * Implements the {@link Property} interface over an {@link Integer}
 * @author yduchesne
 *
 */
public class IntProperty implements Property, Serializable{
  
  static final long serialVersionUID = 1L;
  
  private int value;
  
  public IntProperty(int value) {
    this.value = value;
  }
  
  public boolean getBooleanValue() {
    return value > 0;
  }
  
  public int getIntValue() {
    return value;
  }
  
  public long getLongValue() {
    return value;
  }
  
  public String getValue() {
    return Integer.toString(value);
  }
  
  @Override
  public String toString(){
    return getValue();
  }

}
