package org.sapia.corus.configurator;

import java.util.Iterator;
import java.util.Properties;

import org.sapia.corus.admin.Arg;
import org.sapia.corus.db.DbMap;

public class PropertyStore {

  private DbMap<String, String> properties;

  public PropertyStore(DbMap<String, String> properties) {
    this.properties = properties;
  }
  
  public void addProperty(String name, String value) {
    properties.put(name, value);
  }
  
  public String getProperty(String name) {
    return properties.get(name);
  }
  
  public void removeProperty(String name) {
    properties.remove(name); 
  }

  public void removeProperty(Arg pattern) {
    Iterator<String> names = properties.keys();
    while(names.hasNext()){
      String name = names.next();
      if(pattern.matches(name)){
        removeProperty(name);
      }
    }
  }

  public Properties getProperties() {
    Iterator<String> names = properties.keys();
    Properties props = new Properties();
    while(names.hasNext()){
      String name = names.next();
      props.setProperty(name, properties.get(name));
    }
    return props;
  }

}
