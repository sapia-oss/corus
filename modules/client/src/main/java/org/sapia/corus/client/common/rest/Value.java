package org.sapia.corus.client.common.rest;

import java.util.List;
import java.util.Set;

import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Strings;

/**
 * Holds a parameter value.
 * 
 * @author yduchesne
 *
 */
public class Value {
  
  private String name;
  private String value;
  
  public Value(String name, String value) {
    this.name  = name;
    this.value = value;
  }
  
  /**
   * @return this instance's corresponding parameter name.
   */
  public String getName() {
    return name;
  }
  
  /**
   * @return this instance.
   */
  public Value notNull() throws IllegalArgumentException {
    if (value == null || value.isEmpty()) {
      throw new IllegalArgumentException(String.format("Value expected for %s", name));
    }
    return this;
  }
  
  /**
   * @return <code>true</code> if the value is null.
   */
  public boolean isNull() {
    return value == null;
  }
  
  /**
   * @return <code>true</code> if the value is not null.
   */
  public boolean isSet() {
    return value != null;
  }
  
  /**
   * @return this instance's value, as a {@link String}.
   */
  public String asString() {
    if (value == null) {
      throw new IllegalStateException("Value not set for: " + name);
    }
    return value;
  }
  
  /**
   * @return this instance's value, as a boolean.
   */
  public boolean asBoolean() {
    return 
        value != null && (!value.equals("false") 
        || value.equals("true") || value.equals("1") 
        || value.equals("on") || value.equals("yes"));
  }

  /**
   * @return this instance's value, as an integer.
   */
  public int asInt() {
    if (value == null) {
      throw new NumberFormatException("Value not set for " + name);
    }
    return Integer.parseInt(value);
  }
  
  /**
   * @return this instance's value, parsed as {@link Set} (expecting a comma-delimited list).
   */
  public Set<String> asSet() {
    if (value == null) {
      throw new NumberFormatException("Value not set for " + name);
    }
    return Collects.arrayToSet(value.split(","));
  }
  
  /**
   * @return this instance's value, parsed as {@link List} (expecting a comma-delimited list).
   */
  public List<String> asList() {
    if (value == null) {
      throw new NumberFormatException("Value not set for " + name);
    }
    return Collects.arrayToList(value.split(","));
  }
  
  @Override
  public String toString() {
    return Strings.toString("name", name, "value", value == null ? "null" : value);
  }
   
}
