package org.sapia.corus.client.common.reference;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Strings;

/**
 * Implements an immutable {@link Reference}.
 * 
 * @author yduchesne
 *
 */
public class ImmutableReference<T> implements Reference<T> {
 
  private T instance;
  
  /**
   * DO NOT INVOKE: meant for externalization only.
   */
  public ImmutableReference() {
  }
  
  public ImmutableReference(T instance) {
    Assertions.notNull(instance, "Instance cannot be null");
    this.instance = instance;
  }
  
  @Override
  public T get() {
    return instance;
  }
  
  /**
   * This method ignores the given instance, to preserve immutability.
   * 
   * @param instance some instance to set.
   */
  @Override
  public void set(T instance) {
    // noop
  }
  
  @Override
  public boolean setIf(T newState, T expectedCurrentState) {
    return false;
  }
  
  /**
   * @param other another {@link Reference}.
   * @return a new {@link ImmutableReference}, wrapping the given reference's instance.
   */
  public static <T> ImmutableReference<T> of(Reference<T> other) {
    return new ImmutableReference<T>(other.get());
  }
  
  /**
   * @param instance an instance to wrap.
   * @return a new {@link ImmutableReference}, encapsulating the given instance
   */
  public static <T> ImmutableReference<T> of(T instance) {
    return new ImmutableReference<T>(instance);
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Reference) {
      Reference<?> other = (Reference<?>) obj;
      return instance.equals(other.get());
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return instance.hashCode();
  }
  
  @Override
  public String toString() {
    return Strings.toStringFor(this, "instance", instance);
  }
  
  // --------------------------------------------------------------------------
  // Externalizable
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    instance = (T) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(instance);
  }

}
