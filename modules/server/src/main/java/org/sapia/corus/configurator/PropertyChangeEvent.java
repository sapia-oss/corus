package org.sapia.corus.configurator;

import org.sapia.corus.client.common.NameValuePair;
import org.sapia.corus.client.common.ObjectUtils;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.ubik.rmi.interceptor.Event;

/**
 * An instance of this class is dispatched when a property change occurs.
 */
public class PropertyChangeEvent extends NameValuePair implements Event {

  static final long serialVersionUID = 1L;

  /**
   * Holds the possible event types.
   */
  public enum Type {
    REMOVE, ADD
  }

  // ==========================================================================

  private PropertyScope scope;
  private Type type;
  private String category;

  /**
   * @param name
   *          a property name.
   * @param value
   *          a property value.
   * @param category
   *          a property category.
   * @param scope
   *          a {@link PropertyScope}.
   * @param type
   *          a {@link Type}.
   */
  public PropertyChangeEvent(String name, String value, String category, PropertyScope scope, Type type) {
    super(name, value);
    this.scope    = scope;
    this.category = category;
    this.type     = type;
  }
  
  /**
   * @param name
   *          a property name.
   * @param value
   *          a property value.
   * @param scope
   *          a {@link PropertyScope}.
   * @param type
   *          a {@link Type}.
   */
  public PropertyChangeEvent(String name, String value, PropertyScope scope, Type type) {
    this(name, value, null, scope, type);
  }

  /**
   * @return this instance's {@link PropertyScope}.
   */
  public PropertyScope getScope() {
    return scope;
  }
  
  /**
   * @return this instance's category (if set) in an {@link OptionalValue}.
   */
  public OptionalValue<String> getCategory() {
    return OptionalValue.of(category);
  }

  /**
   * @return this instance's {@link Type}.
   */
  public Type getType() {
    return type;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PropertyChangeEvent) {
      PropertyChangeEvent other = (PropertyChangeEvent) obj;
      return super.equals(obj) 
          && ObjectUtils.safeEquals(other.category, category) 
          && ObjectUtils.safeEquals(other.scope, scope) && ObjectUtils.safeEquals(other.type, type);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return ObjectUtils.safeHashCode(getName(), getValue(), category, scope, type);
  }
}