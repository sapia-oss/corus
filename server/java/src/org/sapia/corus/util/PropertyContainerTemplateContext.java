package org.sapia.corus.util;

/**
 * Implements a {@link TemplateContext} on top of a {@link PropertyContainer}.
 * @author yduchesne
 *
 */
public class PropertyContainerTemplateContext implements TemplateContext{
  
  private PropertyContainer properties;
  
  public PropertyContainerTemplateContext(PropertyContainer properties) {
    this.properties = properties;
  }
  
  public Object get(String propName) {
    return properties.getProperty(propName);
  }

  
  
}
