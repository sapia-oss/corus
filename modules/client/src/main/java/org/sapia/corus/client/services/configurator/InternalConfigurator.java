package org.sapia.corus.client.services.configurator;

import java.util.List;
import java.util.Properties;

import org.sapia.corus.client.common.NameValuePair;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;

/**
 * This interface is used internally within Corus to allow getting 
 * the "unhidden" values of certain properties - has opposed to the {@link Configurator}
 * interfaces, whose implementations should return "hidden" values - see
 * the Javadoc of the {@link Configurator} interface for more on this.
* 
 * @author yduchesne
 *
 */
public interface InternalConfigurator {
  
  /**
   * Returns a property value.
   * 
   * @param name
   * @return the value corresponding to the given property name, or <code>null</code> if
   * no such value exists.
   */
  public String getInternalProperty(String name);
  
  /**
   * Returns a copy of the properties held in this instance.
   * 
   * @param scope a {@link PropertyScope}
   * @return a {@link Properties} instance.
   */
  public Properties getInternalProperties(PropertyScope scope);
  
  /**
   * Returns a list of name/value pairs corresponding to the properties held
   * in this instance.
   * 
   * @param scope a {@link PropertyScope}
   * @return a list of {@link org.sapia.corus.client.common.NameValuePair}
   */
  public List<NameValuePair> getInternalPropertiesAsNameValuePairs(PropertyScope scope);
}
