package org.sapia.corus.configurator;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.services.database.DbMap;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.util.IteratorFilter;
import org.sapia.corus.util.Matcher;

/**
 * An instance of this class wraps a {@link DbMap}, providing the logic around
 * it for storing {@link ConfigProperty} instances.
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
   * @return the names of the properties currently held by this instance.
   */
  public Iterable<String> propertyNames() {
    return new Iterable<String>() { 
      
      @Override
      public Iterator<String> iterator() {
        return properties.keys();
      }
    };
  }
 
  /**
   * @param name
   *          a property name.
   * @param value
   *          a property value.
   */
  public void addProperty(String name, String value) {
    properties.put(name, new ConfigProperty(name, value));
  }

  /**
   * @param name
   *          a property name.
   * @return the property value that corresponds to the given name.
   */
  public String getProperty(String name) {
    ConfigProperty prop = properties.get(name);
    if (prop == null) {
      return null;
    }
    return prop.getValue();
  }

  /**
   * Removes the property with the given name.
   * 
   * @param name
   *          a property name.
   */
  public void removeProperty(String name) {
    properties.remove(name);
  }

  /**
   * Removes the properties matching the given pattern.
   * 
   * @param pattern
   *          an {@link ArgMatcher} instance corresponding to a pattern to test against
   *          property names. All matching properties are removed.
   */
  public void removeProperty(final ArgMatcher pattern) {
    List<ConfigProperty> toRemove = new IteratorFilter<ConfigProperty>(new Matcher<ConfigProperty>() {
      @Override
      public boolean matches(ConfigProperty object) {
        return pattern.matches(object.getName());
      }
    }).filter(properties.values()).get();

    for (ConfigProperty r : toRemove) {
      properties.remove(r.getName());
    }
  }

  /**
   * @return this instance's {@link ConfigProperty}, in a {@link Properties}
   *         object.
   */
  public Properties getProperties() {
    Iterator<String> names = properties.keys();
    Properties props = new Properties();
    while (names.hasNext()) {
      String name = names.next();
      ConfigProperty prop = properties.get(name);
      if (prop != null && prop.getValue() != null) {
        props.setProperty(name, prop.getValue());
      }
    }
    return props;
  }

  /**
   * Archives all the properties currently contained.
   * 
   * @param revId the revision ID to use when archiving.
   */
  public void archive(RevId revId) {
    properties.clearArchive(revId);
    Iterator<String> names = properties.keys();
    while (names.hasNext()) {
      String name = names.next();
      properties.archive(revId, name);
    }
  }
  
  /**
   * @param revId unarchives the properties corresponding to the given revision ID.
   */
  public void unarchive(RevId revId) {
    properties.unarchive(revId);
  }
  
  @Override
  public String toString() {
    return properties.toString();
  }

}
