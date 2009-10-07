package org.sapia.corus.util;

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
  
  public String getValue() {
    return value;
  }
  
  public String getName() {
    return name;
  }

  public int compareTo(NameValuePair o) {
    return name.compareTo(o.getName());
  }
}
