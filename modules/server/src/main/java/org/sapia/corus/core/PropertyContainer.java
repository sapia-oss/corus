package org.sapia.corus.core;

/**
 * Specifies the behavior for looking up property values, given their names.
 * 
 * @author yduchesne
 * 
 */
public interface PropertyContainer {

  /**
   * @param name
   *          a property name.
   * @return the property value corresponding to the given name.
   */
  public String getProperty(String name);

}
