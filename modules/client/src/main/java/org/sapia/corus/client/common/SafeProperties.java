package org.sapia.corus.client.common;

import java.util.Properties;

import org.sapia.ubik.util.Assertions;

/**
 * Extends the {@link Properties} class to provide safe operations around properties.
 * 
 * @author yduchesne
 *
 */
public class SafeProperties extends Properties {
  
  static final long serialVersionUID = 1L;

  public SafeProperties() {
  }
  
  public SafeProperties(Properties parent) {
    super(parent);
  }
  
  /**
   * 
   * @param key a property key, whose corresponding value should be returned.
   * @return the property value corresponding to the given key.
   * @throws IllegalArgumentException if no such property value exists for the given key.
   */
  public String getPropertyNotNull(String key) throws IllegalArgumentException {
    String value = super.getProperty(key);
    Assertions.notNull(value, "No property found for %s", key);
    return value;
  }

  /**
   * @param key a property key, whose corresponding optional value should be returned.
   * @return the optional property value corresponding to the given key.
   */
  public OptionalValue<String> getOptionalProperty(String key) {
    return OptionalValue.of(super.getProperty(key));
  }
}
