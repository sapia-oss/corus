package org.sapia.corus.util;

import java.io.Serializable;

/**
 * Implements the {@link Property} interface over a {@link Long}
 * 
 * @author yduchesne
 *
 */
public class LongProperty implements Property, Serializable{
  
  static final long serialVersionUID = 1L;
  
  private long value;
  
  public LongProperty(long value) {
    this.value = value;
  }
  
  public String getValue() {
    return Long.toString(value);
  }
  
  public boolean getBooleanValue() {
    return value > 0;
  }
  
  public int getIntValue() {
    return (int)value;
  }
  
  public long getLongValue() {
    return value;
  }
  
  @Override
  public String toString() {
    return getValue();
  }

}
