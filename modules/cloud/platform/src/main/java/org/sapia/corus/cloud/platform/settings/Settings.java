package org.sapia.corus.cloud.platform.settings;

/**
 * Specifies the behavior for retrieving arbitrary settings.
 * 
 * 
 * @author yduchesne
 *
 */
public interface Settings {

  /**
   * @param name the name of the desired setting.
   * @return the {@link Setting} with the given name.
   * @throws MissingSettingException if no such setting exists.
   */
  public Setting getNotNull(String name) throws MissingSettingException;
  
  /**
   * @param name the name of the desired setting.
   * @param defaultValue a default value.
   * @return the {@link Setting} corresponding to the given name, or a setting encapsulating
   * the given default value if this instance does not actually old a setting for the
   * provided name.
   */
  public <T> Setting get(String name, T defaultValue);
  
  /**
   * @param name the name of the desired setting.
   * @return the {@link Setting} corresponding to the given name - if no such setting
   * actually exists, a {@link Setting} will still be returned, but with a value of <code>null</code>.
   * 
   * @see Setting#isNull().
   */
  public Setting get(String name);
  
  /**
   * @param name the name of the setting to set.
   * @param value the value of the setting to set.
   */
  public <T> void set(String name, T value);
  
  // ==========================================================================
  
  public class NullSettings implements Settings {
    
    @Override
    public Setting get(String name) {
      return new SettingImpl(name, null);
    }
    
    @Override
    public <T> Setting get(String name, T defaultValue) {
      return new SettingImpl(name, defaultValue);
    }
    
    @Override
    public Setting getNotNull(String name) throws MissingSettingException {
      throw new MissingSettingException(String.format("Setting %s has no value", name));
    }
    
    @Override
    public <T> void set(String name, T value) {
    }
    
  }
}
