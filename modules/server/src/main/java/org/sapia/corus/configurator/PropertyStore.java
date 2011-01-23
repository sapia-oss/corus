package org.sapia.corus.configurator;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.services.db.DbMap;
import org.sapia.corus.util.IteratorFilter;
import org.sapia.corus.util.Matcher;

public class PropertyStore {

  private DbMap<String, ConfigProperty> properties;

  public PropertyStore(DbMap<String, ConfigProperty> properties) {
    this.properties = properties;
  }
  
  public void addProperty(String name, String value) {
    properties.put(name, new ConfigProperty(name, value));
  }
  
  public String getProperty(String name) {
    ConfigProperty prop = properties.get(name);
    if(prop == null){
      return null;
    }
    return prop.getValue();
  }
  
  public void removeProperty(String name) {
    properties.remove(name); 
  }

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
