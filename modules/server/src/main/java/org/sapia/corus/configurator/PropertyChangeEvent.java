package org.sapia.corus.configurator;

import org.sapia.corus.client.common.NameValuePair;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.ubik.rmi.interceptor.Event;

/**
 * An instance of this class is dispatched when a property change occurs.
 */
public class PropertyChangeEvent extends NameValuePair implements Event {
  
  public enum Type {
    REMOVE,
    ADD
  }
  
  private PropertyScope scope;
  private Type type;
  
  public PropertyChangeEvent(String name, String value, PropertyScope scope, Type type) {
    super(name, value);
    this.scope = scope;
    this.type = type;
  }

  public PropertyScope getScope() {
    return scope;
  }
  
  public Type getType() {
    return type;
  }
}