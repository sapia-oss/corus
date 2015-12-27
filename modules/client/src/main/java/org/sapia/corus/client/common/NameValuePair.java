package org.sapia.corus.client.common;

import java.io.Serializable;

import org.sapia.ubik.util.Strings;

/**
 * This class models a name/value pair.
 * 
 * @author yduchesne
 * 
 */
public class NameValuePair implements Serializable, Comparable<NameValuePair>, Matcheable {

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
  
  // --------------------------------------------------------------------------

  @Override
  public int compareTo(NameValuePair o) {
    return name.compareTo(o.getName());
  }
  
  @Override
  public boolean matches(Pattern pattern) {
    return value == null ? false : pattern.matches(value);
  }

  @Override
  public String toString() {
    return Strings.toStringFor(this, "name", name, "value", value);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof NameValuePair) {
      NameValuePair other = (NameValuePair) obj;
      return ObjectUtil.safeEquals(name, other.name) && ObjectUtil.safeEquals(value, other.value);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return ObjectUtil.safeHashCode(name, value);
  }

}
