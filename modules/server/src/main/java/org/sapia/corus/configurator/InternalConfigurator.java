package org.sapia.corus.configurator;

import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.util.DynamicProperty;

/**
 * Extends the {@link Configurator} interface with internal, server-side behavior.
 * 
 * @author yduchesne
 *
 */
public interface InternalConfigurator extends Configurator {

  /**
   * @param propertyName the name of the property to register for.
   * @param dynProperty the {@link DynamicProperty} to update if a change is detected.
   */
  public <T> void registerForPropertyChange(String propertyName, DynamicProperty<T> dynProperty);

}
