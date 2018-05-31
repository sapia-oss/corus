package org.sapia.corus.client.common.reference;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Strings;
import org.sapia.ubik.util.SysClock;
import org.sapia.ubik.util.SysClock.RealtimeClock;
import org.sapia.ubik.util.TimeValue;

/**
 * Auto-resets to its default state passed a given amount of idle time.
 * <p>
 * This class is thread-safe, providing fully synchronized behavior.
 * 
 * @author yduchesne
 *
 */
public class AutoResetReference<T> implements Reference<T> {
  
  private transient SysClock sysClock = RealtimeClock.getInstance();
  
  private T                 defaultInstance;
  private volatile boolean  isDefault = true;
  private volatile T        currentInstance;
  private long              lastChangeMillis;
  private long              idleTimeDelayMillis;
 
  /**
   * DO NOT INVOKE: meant for externalization only.
   */
  public AutoResetReference() {
  }
  
  public AutoResetReference(T instance, TimeValue idleTimeDelay) {
    this(instance, instance, idleTimeDelay);
  }
  
  public AutoResetReference(T defaultInstance, T instance, TimeValue idleTimeDelay) {
    Assertions.notNull(defaultInstance, "Default value cannot be null");
    Assertions.notNull(instance, "Value cannot be null");
    Assertions.notNull(idleTimeDelay, "Time delay cannot be null");


    this.defaultInstance     = instance;
    this.currentInstance     = instance;
    this.idleTimeDelayMillis = idleTimeDelay.getValueInMillis();
  }
 
  // provided for testing 
  void setClock(SysClock clock) {
    this.sysClock = clock;
  }
   
  @Override
  public synchronized void set(T instance) {
    Assertions.notNull(instance, "Value cannot be null");
    isDefault        = false;
    currentInstance  = instance;
    lastChangeMillis = sysClock.currentTimeMillis();
  }
  
  @Override
  public synchronized boolean setIf(T newState, T expectedCurrentState) {
    if (currentInstance.equals(expectedCurrentState)) {
      set(newState);
      return true;
    }
    return false;
  }
  
  @Override
  public synchronized T get() {
    if (isDefault) {
      return currentInstance;
    } else if(sysClock.currentTimeMillis() - lastChangeMillis > idleTimeDelayMillis) {
      isDefault = true;
      currentInstance = defaultInstance;
      lastChangeMillis = sysClock.currentTimeMillis();
      return currentInstance;
    }
    return currentInstance;
  }
  
  // --------------------------------------------------------------------------
  // Externalizable interface
  
  @SuppressWarnings("unchecked")
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    sysClock = RealtimeClock.getInstance();
    this.defaultInstance      = (T) in.readObject();
    this.isDefault            = in.readBoolean();
    this.currentInstance      = (T) in.readObject();
    this.idleTimeDelayMillis  = in.readLong();
  }
  
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(defaultInstance);
    out.writeBoolean(isDefault);
    out.writeObject(currentInstance);
    out.writeLong(idleTimeDelayMillis);
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public int hashCode() {
    return currentInstance.hashCode();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Reference) {
      Reference<?> other = (Reference<?>) obj;
      return currentInstance.equals(other.get());
    }
    return false;
  }
  
  @Override
  public String toString() {
    return Strings.toStringFor(this, "currentInstance", currentInstance, "defaultInstance", defaultInstance);
  }

}
