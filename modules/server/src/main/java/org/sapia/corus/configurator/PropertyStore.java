package org.sapia.corus.configurator;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.util.IteratorFilter;
import org.sapia.corus.util.Matcher;

/**
 * An instance of this class wraps a {@link DbMap}, providing the logic around it
 * for storing {@link ConfigProperty} instances.
 * 
 * @author yduchesne
 *
 */
public class PropertyStore {

  private DbMap<String, ConfigProperty> properties;
  
  public PropertyStore(DbMap<String, ConfigProperty> properties) {
    this.properties = properties;
  }
  
  /**
   * @param name a property name.
   * @param value a property value.
   */
  public void addProperty(String name, String value) {
    properties.put(name, new ConfigProperty(name, value));
  }
  
  /**
   * @param name a property name.
   * @return the property value that corresponds to the given name.
   */
  public String getProperty(String name) {
    ConfigProperty prop = properties.get(name);
    if(prop == null){
      return null;
    }
    return prop.getValue();
  }
  
  /**
   * Removes the property with the given name.
   * 
   * @param name a property name.
   */
  public void removeProperty(String name) {
    properties.remove(name); 
  }

  /**
   * Removes the properties matching the given pattern.
   * 
   * @param pattern an {@link Arg} instance corresponding to a pattern
   * to test against property names. All matching properties are removed.
   */
  public void removeProperty(final Arg pattern) {
    List<ConfigProperty> toRemove = new IteratorFilter<ConfigProperty>(
        new Matcher<ConfigProperty>() {
          @Override
          public boolean matches(ConfigProperty object) {
            return pattern.matches(object.getName());
          }
        }
    ).filter(properties.values()).get();

    for(ConfigProperty r:toRemove){
      properties.remove(r.getName());
    }
  }

  /**
   * @return this instance's {@link ConfigProperty}, in a {@link Properties}
   * object.
   */
  public Properties getProperties() {
    Iterator<String> names = properties.keys();
    Properties props = new Properties();
    while(names.hasNext()){
      String name = names.next();
      ConfigProperty prop = properties.get(name);
      if(prop != null && prop.getValue() != null){
        props.setProperty(name, prop.getValue());
      }
    }
    return props;
  }

}
