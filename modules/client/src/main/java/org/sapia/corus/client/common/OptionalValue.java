package org.sapia.corus.client.common;

import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Strings;

/**
 * Wraps a given value, offering introspection methods to avoid {@link NullPointerException}s.
 * 
 * @author yduchesne
 *
 * @param <T> the generic type corresponding to the type of value that an instance of this
 * class encapsulates.
 */
public class OptionalValue<T> {

  private T value;
  
  /**
   * @param value a value to wrap.
   */
  public OptionalValue(T value) {
    this.value = value;
  }
  
  /**
   * @return <code>true</code> if this instance's value is <code>null</code>.
   */
  public boolean isNull() {
    return value == null;
  }
  
  /**
   * 
   * @return <code>true</code> if this instance's value is not <code>null</code>.
   */
  public boolean isSet() {
    return value != null;
  }
  
  /**
   * @return the value encapsulated by this instance.
   */
  public T get() {
    Assertions.illegalState(value == null, 
        "Value is not set. Call isNull() or isSet() to test for value presence");
    return value;
  }
  
  /**
   * @param value a value to wrap.
   * @return a new instance of this class, wrapping the given value.
   */
  public static <T> OptionalValue<T> of(T value) {
    return new OptionalValue<>(value);
  }
  
  @Override
  public String toString() {
    return Strings.toStringFor(this, "value", value);
  }
  
}
