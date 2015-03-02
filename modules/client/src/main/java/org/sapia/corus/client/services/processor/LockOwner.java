package org.sapia.corus.client.services.processor;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.util.Strings;

public class LockOwner implements Externalizable {

  static final long serialVersionUID = 1L;
  static int counter;

  private int     id;
  private boolean exclusive = true;

  public LockOwner() {
    id = increment();
  }

  static synchronized int increment() {
    return counter++;
  }

  /**
   * Creates a new instance of this class.
   * 
   * @return a new {@link LockOwner} instance.
   */
  public static LockOwner createInstance() {
    return new LockOwner();
  }
  
  /**
   * Sets this instance's <code>exclusive</code> flag to false.
   * 
   * @return this instance.
   */
  public LockOwner nonExclusive() {
    exclusive = false;
    return this;
  }
  
  /**
   * @return <code>true</code> if this instance should have exclusive access to {@link ProcessLock}s.
   */
  public boolean isExclusive() {
    return exclusive;
  }
 
  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof LockOwner) {
      return ((LockOwner) obj).id == id;
    } else {
      return false;
    }
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    id = in.readInt();
    exclusive = in.readBoolean();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeInt(id);
    out.writeBoolean(exclusive);
  }
  
  @Override
  public String toString() {
    return Strings.toString("id", id, "exclusive", exclusive);
  }
}
