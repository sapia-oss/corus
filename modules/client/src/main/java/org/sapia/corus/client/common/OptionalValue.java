package org.sapia.corus.client.common;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

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
public class OptionalValue<T> implements Externalizable {

  static final long serialVersionUID = 1L;
  
  private T value;

  /**
   * Do not use: meant for externalization only.
   */
  public OptionalValue() {
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    value = (T) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(value);
  }
  
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
  
  /**
   * @return an {@link OptionalValue} encapsulating a <code>null</code> reference.
   */
  public static <T> OptionalValue<T> none() {
    return new OptionalValue<T>(null);
  }
  
  // --------------------------------------------------------------------------
  // Object override
  
  @Override
  public String toString() {
    return Strings.toStringFor(this, "value", value);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof OptionalValue) {
      OptionalValue<?> other = OptionalValue.class.cast(obj);
      if (value == null || other.value == null) {
        return false;
      } else {
        return value.equals(other.value);
      }
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return value == null ? super.hashCode() : value.hashCode();
  }
  
}
