package org.sapia.corus.util;

import java.util.HashMap;
import java.util.Map;

/**
 * An instance of this class transform a property into another.
 * 
 * @author yduchesne
 * 
 */
public interface PropertiesTransformer {

  public static class Property {

    String name, value;

  }

  public class MappedPropertiesTransformer implements PropertiesTransformer {

    private Map<String, String> propertyNames = new HashMap<String, String>();

    public MappedPropertiesTransformer add(String nameToTransform, String substitutionName) {
      propertyNames.put(nameToTransform, substitutionName);
      return this;
    }

    @Override
    public boolean accepts(String name, String value) {
      return propertyNames.containsKey(name);
    }

    @Override
    public Property transfrom(String name, String value) {
      String otherName = propertyNames.get(name);
      if (otherName == null) {
        otherName = name;
      }
      Property prop = new Property();
      prop.name = otherName;
      prop.value = value;
      return prop;
    }

    public static MappedPropertiesTransformer createInstance() {
      return new MappedPropertiesTransformer();
    }

  }

  /**
   * @param name
   *          a property name.
   * @param value
   *          a property value.
   * @return <code>true</code> if this instance accepts to transform the given
   *         property.
   */
  public boolean accepts(String name, String value);

  /**
   * @param name
   *          the name of the property to transform.
   * @param value
   *          the value of the property to transform.
   * @return the property.
   */
  public Property transfrom(String name, String value);
}
