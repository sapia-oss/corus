package org.sapia.corus.cloud.platform.settings;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Default implementation of the {@link Setting} interface.
 * 
 * @author yduchesne
 *
 */
public class SettingImpl implements Setting {

  private String name;
  private Object value;
  
  public SettingImpl(String name, Object value) {
    this.name  = name;
    this.value = value;
  }
  
  @Override
  public String getName() {
    return name;
  }
  
  @Override
  public boolean isNull() {
    return value == null;
  }
  
  @Override
  public boolean isSet() {
    return value != null;
  }
  
  @Override
  public <T> T get(Class<T> type) throws MissingSettingException {
    if (value == null) {
      throw new MissingSettingException(String.format("Setting %s has no value", name));
    }
    
    if (!type.isAssignableFrom(value.getClass())) {
      throw new IllegalStateException(String.format("Value has type: %s, but trying to cast to %s", 
          value.getClass().getName(), type.getName()));
    }
    return type.cast(value);
  }

  @Override
  public <T> T get(Class<T> type, T defaultValue) {
    if (value == null) {
      return defaultValue;
    } else {
      return get(type);
    }
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public <T> List<T> getListOf(Class<T> elementType) throws MissingSettingException {
    if (value == null) {
      throw new MissingSettingException(String.format("Setting %s has no value", name));
    }
    if (!List.class.isAssignableFrom(value.getClass())) {
      throw new IllegalStateException(String.format("Value has type: %s, but trying to cast to list of %s", 
          value.getClass().getName(), elementType.getName()));
    }
    return (List<T>) value;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public <T> Set<T> getSetOf(Class<T> elementType)
      throws MissingSettingException {
    if (value == null) {
      throw new MissingSettingException(String.format("Setting %s has no value", name));
    }
    if (!Set.class.isAssignableFrom(value.getClass())) {
      throw new IllegalStateException(String.format("Value has type: %s, but trying to cast to set of %s", 
          value.getClass().getName(), elementType.getName()));
    }
    return (Set<T>) value;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public <K, V> Map<K, V> getMapOf(Class<K> keyType, Class<V> valueType)
      throws MissingSettingException {
    if (value == null) {
      throw new MissingSettingException(String.format("Setting %s has no value", name));
    }
    if (!Map.class.isAssignableFrom(value.getClass())) {
      throw new IllegalStateException(String.format("Value has type: %s, but trying to cast to Map", 
          value.getClass().getName()));
    }
    return (Map<K, V>) value;
  }
  
  @Override
  public String toString() {
    return "[" + name + "=" + value + "]";
  }
}
