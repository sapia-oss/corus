package org.sapia.corus.cloud.platform.settings;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of the {@link Settings} interface.
 * 
 * @author yduchesne
 *
 */
public class SettingsImpl implements Settings {
  
  private Settings            parent;
  private Map<String, Object> settings = new HashMap<String, Object>();
  
  /**
   * @param parent the parent {@link Settings}, which are overridden by this instance - and
   * used as a fallback when settings are not found in this instance.
   * @param settings the actual setting values.
   */
  public SettingsImpl(Settings parent, Map<String, Object> settings) {
    this.parent   = parent;
    this.settings = settings;
  }
  
  /**
   * @param settings the actual setting values.
   */
  public SettingsImpl(Map<String, Object> settings) {
    this(new NullSettings(), settings);
  }
  
  @Override
  public <T> Setting get(String name, T defaultValue) {
    Object val  = settings.get(name);
    if (val == null) {
      Setting s = parent.get(name);
      if (!s.isNull()) {
        return s;
      } else {
        val = defaultValue;
      }
    }
    return new SettingImpl(name, val);
  }
  
  @Override
  public Setting getNotNull(String name) throws MissingSettingException {
    Object val  = settings.get(name);
    if (val == null) {
      Setting s = parent.get(name);
      if (!s.isNull()) {
        return s;
      } else {
        throw new MissingSettingException(String.format("Setting %s has no value", name));
      }
    }
    return new SettingImpl(name, val);
  }
  
  @Override
  public Setting get(String name) {
    Object val = settings.get(name);
    if (val == null) {
      return parent.get(name);
    }
    return new SettingImpl(name, val);
  }

  @Override
  public <T> void set(String name, T value) {
    settings.put(name, value);
  }
}
