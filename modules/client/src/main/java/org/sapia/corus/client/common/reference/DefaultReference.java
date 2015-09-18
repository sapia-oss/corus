package org.sapia.corus.client.common.reference;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Strings;

/**
 * Holds an arbitrary object reference.
 * 
 * @author yduchesne
 */
public class DefaultReference<T> implements Reference<T> {

  private volatile T instance;
  
  /**
   * DO NOT INVOKE. Meant for externalization only.
   */
  public DefaultReference() {
  }
  
  public DefaultReference(T instance) {
    Assertions.notNull(instance, "Instance cannot be null");
    this.instance = instance;
  }
  
  @Override
  public T get() {
    return instance;
  }

  @Override
  public void set(T instance) {
    this.instance = instance;
  }
  
  /**
   * @param instance an arbitrary object to wrap.
   * @return a new instance of this class, wrapping the given object.
   */
  public static <T> Reference<T> of(T instance) {
    return new DefaultReference<T>(instance);
  }
 
  // --------------------------------------------------------------------------
  // Externalizable interface.
  
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
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public int hashCode() {
    return instance.hashCode();
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof DefaultReference) {
      Reference<T> other = (Reference<T>) obj;
      return instance.equals(other.get());
    }
    return false;
  }
  
  @Override
  public String toString() {
    return Strings.toStringFor(this, "instance", instance);
  }
  
}
