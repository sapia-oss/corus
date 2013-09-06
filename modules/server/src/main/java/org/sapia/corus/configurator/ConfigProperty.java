package org.sapia.corus.configurator;

import org.sapia.corus.client.common.ObjectUtils;


/**
 * Models a configuration property, corresponding to a name and its associated value.
 * 
 * @author yduchesne
 *
 */
public class ConfigProperty {
  
  private String name, value;
  
  ConfigProperty() {
  }

  ConfigProperty(String name, String value){
    this.name = name;
    this.value = value;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public void setValue(String value) {
    this.value = value;
  }
  
  public String getValue() {
    return value;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ConfigProperty) {
      ConfigProperty other = (ConfigProperty) obj;
      return ObjectUtils.safeEquals(other.name, name)
          && ObjectUtils.safeEquals(other.value, value);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return ObjectUtils.safeHashCode(name, value); 
  }

}
