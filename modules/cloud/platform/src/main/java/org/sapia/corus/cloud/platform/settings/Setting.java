package org.sapia.corus.cloud.platform.settings;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Defines the behavior of a setting, which has a name and a typed value.
 * 
 * @author yduchesne
 *
 */
public interface Setting {

  /**
   * @return the name of this setting.
   */
  public String getName();
  
  /**
   * @param type the type to which to cast this setting's value.
   * @return this instance's value.
   * @throws MissingSettingException if this setting actually has no value.
   */
  public <T> T get(Class<T> type) throws MissingSettingException;
  
  /**
   * @param type the type to which to cast this setting's value.
   * @param defaultValue a default value to use, if this instance has no value.
   * @return this instance's value.
   */
  public <T> T get(Class<T> type, T defaultValue);
  
  /**
   * @param elementType the {@link Class} corresponding to the type of the elements in the expected list.
   * @return a {@link List}.
   * @throws MissingSettingException if this setting actually has no value.
   */
  public <T> List<T> getListOf(Class<T> elementType) throws MissingSettingException;
  
  /**
   * @param elementType the {@link Class} corresponding to the type of the elements in the expected set.
   * @return a {@link Set}.
   * @throws MissingSettingException if this setting actually has no value.
   */
  public <T> Set<T> getSetOf(Class<T> elementType) throws MissingSettingException;
  
  /**
   * @param keyType the {@link Class} corresponding to the type of the keys in the expected map.
   * @return a {@link Map}.
   * @throws MissingSettingException if this setting actually has no value.
   */
  public <K, V> Map<K, V> getMapOf(Class<K> keyType, Class<V> valueType) throws MissingSettingException;
  
  /**
   * @return <code>true</code> if this instance has no value.
   */
  public boolean isNull();
  
  /**
   * @return <code>true</code> if this instance has a value.
   */
  public boolean isSet();
  
}
