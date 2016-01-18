package org.sapia.corus.util;

import java.util.ArrayList;
import java.util.List;

import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Strings;

/**
 * This class provides the behavior for keeping instance data that can be modified in the course of its lifetime.
 * It offers behavior for notifying upon state change, offering potential listeners the possibility of taking into
 * account such changes in the context of their own logic.
 * <p>
 * Concretely, such behavior is convenient in the context of allowing certain Corus configuration properties to be
 * updated dynamically, without requiring a server restart.
 * 
 * @author yduchesne
 *
 * @param <T> the type of the property.
 */
public class DynamicProperty<T> {
  
  /**
   * Notified when a {@link DynamicProperty} is modified.
   *  
   * @author yduchesne
   *
   * @param <T> the type of the property.
   */
  public interface DynamicPropertyListener<T> {
    
    /**
     * @param property the {@link DynamicProperty} that's been modified.
     */
    public void onModified(DynamicProperty<T> property);
    
  }
  
  // --------------------------------------------------------------------------
  
  private Class<T>   type;
  private volatile T value;
  private List<DynamicPropertyListener<T>> listeners = new ArrayList<DynamicProperty.DynamicPropertyListener<T>>();
  
  /**
   * Creates a new instance of this class, without a value.
   */
  public DynamicProperty(Class<T> type) {
    this.type = type;
  }
  
  /**
   * Creates a new instance of this class, with the given a value.
   * 
   * @param value this instance's value.
   */
  @SuppressWarnings("unchecked")
  public DynamicProperty(T value) {
    this((Class<T>) value.getClass());
    this.value = value;
  } 
  
  /**
   * @return the {@link Class} corresponding to the type of value that this property holds.
   */
  public Class<T> getType() {
    return type;
  }
  
  /**
   * @return this instance's value, or <code>null</code> if no such value exists.
   */
  public T getValue() {
    return value;
  }
  
  /**
   * This method notifies the {@link DynamicPropertyListener}s that have been added to this instance. Notification
   * is done in the thread of the caller.
   * 
   * @param value a value.
   */
  public void setValue(T value) {
    Assertions.isTrue(
        type.isAssignableFrom(value.getClass()), "Expected instance of %s, got %s (instance of %s)", 
        type.getName(), value, value.getClass().getName()
    );
    this.value = value;
    for (DynamicPropertyListener<T> l : listeners) {
      l.onModified(this);
    }
  }
  
  /**
   * @param listener a {@link DynamicPropertyListener} to add.
   */
  public DynamicProperty<T> addListener(DynamicPropertyListener<T> listener) {
    synchronized (listeners) {
      listeners.add(listener);
    }
    return this;
  }
  
  /**
   * @param listener a {@link DynamicPropertyListener} to remove.
   */
  public void removeListener(DynamicPropertyListener<T> listener) {
    synchronized (listeners) {
      listeners.remove(listener);
    }
  }
  
  /**
   * @return this instance's value.
   * @throws IllegalStateException if no such value is set.
   */
  public T getValueNotNull() throws IllegalStateException {
    if (value == null) {
      throw new IllegalStateException("Value not set");
    }
    return value;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof DynamicProperty) {
      DynamicProperty<?> other = (DynamicProperty<?>) obj;
      if (this == obj) {
        return true;
      } else if (value == null && other.value == null) {
        return true;
      } else if (value == null || other.value == null) {
        return false;
      } else if (value == other.value) {
        return true;
      } else {
        return value.equals(other.value);
      }
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    if (value == null) {
      return 0;
    } else {
      return value.hashCode();
    }
  }
  
  @Override
  public String toString() {
    return Strings.toString("type", type, "value", value);
  }
  
}
