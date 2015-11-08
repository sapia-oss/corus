package org.sapia.corus.cloud.platform.settings;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ReflectionSettings implements Settings {

  class FieldInfo {
    
    private Object owner;
    private Field  field;
    
    private FieldInfo(Object owner, Field field) {
      this.owner = owner;
      this.field = field;
    }
    
    private Object get() {
      try {
        return field.get(owner);
      } catch (Exception e) {
        throw new IllegalStateException("Could not acquire value for field: " + field.getName(), e);
      }
    }
    
    private void set(Object value) {
      try {
        field.set(owner, value);
      } catch (Exception e) {
        throw new IllegalStateException("Could not set value for field: " + field.getName(), e);
      }      
    }
  }
  
  // =========================================================================-
  
 
  private Map<String, FieldInfo> fieldsNyName = new HashMap<String, FieldInfo>();
  private Map<String, Object>    newSettings  = new HashMap<String, Object>();
  
  public ReflectionSettings(Object...delegates) {
    for (Object delegate : delegates) {
      for (Field f : delegate.getClass().getDeclaredFields()) {
        if (!Modifier.isStatic(f.getModifiers())) {
          if (fieldsNyName.containsKey(f.getName())) {
            throw new IllegalStateException("Field duplication for %" + f.getName());
          }
          f.setAccessible(true);
          fieldsNyName.put(f.getName(), new FieldInfo(delegate, f));
        }
      }
    }
  }
  
  @Override
  public Setting get(String name) {
    FieldInfo info = fieldsNyName.get(name);
    if (info != null) {
      return new SettingImpl(name, info.get());
    }
    return new SettingImpl(name, newSettings.get(name));
  }
  
  @Override
  public <T> Setting get(String name, T defaultValue) {
    FieldInfo info = fieldsNyName.get(name);
    Object val = null;
    if (info != null) {
      val = info.get();
    }
    if (val == null) {
      val = newSettings.get(name);
    }
    if (val == null) {
      val = defaultValue;
    }
    return new SettingImpl(name, val);
  }
  
  @Override
  public Setting getNotNull(String name) throws MissingSettingException {
    FieldInfo info = fieldsNyName.get(name);
    Object val = null;
    if (info != null) {
      val = info.get();
    }
    if (val == null) {
      val = newSettings.get(val);
    }
    if (val == null) {
      throw new MissingSettingException(String.format("Setting %s has no value", name));
    }
    return new SettingImpl(name, val);
  }
  
  @Override
  public <T> void set(String name, T value) {
    FieldInfo info = fieldsNyName.get(name);
    if (info != null) {
      info.set(value);
    } else {
      newSettings.put(name, value);
    }
  }
  
}
