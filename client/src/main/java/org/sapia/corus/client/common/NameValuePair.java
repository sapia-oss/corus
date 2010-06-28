package org.sapia.corus.client.common;

import java.io.Serializable;

/**
 * This class models a name/value pair.
 * 
 * @author yduchesne
 *
 */
public class NameValuePair implements Serializable, Comparable<NameValuePair>{
  
  static final long serialVersionUID = 1L;
  
  private String name, value;
  
  public NameValuePair(String name, String value) {
    this.name = name;
    this.value = value;
  }
  
  /**
   * @return this instance's value.
   */
  public String getValue() {
    return value;
  }
  
  /**
   * @return this instance's name.
   */
  public String getName() {
    return name;
  }

  public int compareTo(NameValuePair o) {
    return name.compareTo(o.getName());
  }
}
