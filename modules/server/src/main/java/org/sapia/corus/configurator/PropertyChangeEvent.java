package org.sapia.corus.configurator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.sapia.corus.client.common.ObjectUtils;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.configurator.Property;
import org.sapia.ubik.rmi.interceptor.Event;
import org.sapia.ubik.util.Strings;

/**
 * An instance of this class is dispatched when a property change occurs.
 */
public class PropertyChangeEvent implements Serializable, Event {

  private static final long serialVersionUID = -674312933742477636L;

  /**
   * Holds the possible event types.
   */
  public enum EventType {
    ADD,
    REMOVE
  }

  // ==========================================================================

  private EventType eventType;
  private List<Property> properties = new ArrayList<>();
  private PropertyScope scope;

  /**
   * @param eventType a {@link EventType}.
   * @param scope a {@link PropertyScope}.
   */
  public PropertyChangeEvent(EventType eventType, PropertyScope scope) {
    this.eventType = eventType;
    this.scope = scope;
  }

  /**
   * @param eventType an {@link EventType}.
   * @param name a property name.
   * @param value a property value.
   * @param scope a {@link PropertyScope}.
   */
  public PropertyChangeEvent(EventType eventType, String name, String value, PropertyScope scope) {
    this(eventType, name, value, null, scope);
  }

  /**
   * @param eventType an {@link EventType}.
   * @param name a property name.
   * @param value a property value.
   * @param category a property category.
   * @param scope a {@link PropertyScope}.
   */
  public PropertyChangeEvent(EventType eventType, String name, String value, String category, PropertyScope scope) {
    this.eventType = eventType;
    this.scope = scope;
    properties.add(new Property(name, value, category));
  }

  /**
   * @param eventType an {@link EventType}.
   * @param scope a {@link PropertyScope}.
   * @param properties a collection of {@link Property}.
   */
  public PropertyChangeEvent(EventType eventType, PropertyScope scope, Collection<Property> properties) {
    this.eventType = eventType;
    this.scope = scope;
    this.properties.addAll(properties);
  }

  /**
   * @return this instance's {@link EventType}.
   */
  public EventType getEventType() {
    return eventType;
  }

  /**
   * @return this instance's {@link PropertyScope}.
   */
  public PropertyScope getScope() {
    return scope;
  }

  /**
   * Adds the passed in property.
   * 
   * @param property The property to add.
   */
  public void addProperty(Property property) {
    properties.add(property);
  }
  
  /**
   * @param propertyName The property name to look for.
   * @return True if the property exists, false otherwise.
   */
  public boolean containsProperty(String propertyName) {
    boolean isFound = false;
    for (Iterator<Property> it = properties.iterator(); it.hasNext() && !isFound; ) {
      Property property = it.next();
      isFound = property.getName().equals(propertyName);
    }
    
    return isFound;
  }

  /**
   * @return The properties of this event.
   */
  public Collection<Property> getProperties() {
    return properties;
  }
  
  /**
   * @param propertyName The property name to look for.
   * @return The first property of this event that has the given name.
   */
  public Property getFirstPropertyFor(String propertyName) {
    Property result = null;
    for (Iterator<Property> it = properties.iterator(); it.hasNext() && result == null; ) {
      result = it.next();
      if (!result.getName().equals(propertyName)) {
        result = null;
      }
    }
    
    return result;
  }
  
  /**
   * @param propertyName The property name to look for.
   * @return All the properties of this event with the given name.
   */
  public Collection<Property> getPropertiesFor(String propertyName) {
    List<Property> result = new ArrayList<>();
    for (Iterator<Property> it = properties.iterator(); it.hasNext(); ) {
      Property property = it.next();
      if (!property.getName().equals(propertyName)) {
        result.add(property);
      }
    }
    
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PropertyChangeEvent) {
      PropertyChangeEvent other = (PropertyChangeEvent) obj;
      return ObjectUtils.safeEquals(eventType, other.eventType)
          && ObjectUtils.safeEquals(other.scope, scope)
          && ObjectUtils.safeEquals(other.properties, properties);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return ObjectUtils.safeHashCode(eventType, scope, properties);
  }
  
  @Override
  public String toString() {
    return Strings.toString("eventType", eventType, "scope", scope, "properties", properties);
  }

}