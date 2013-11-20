package org.sapia.corus.client.services.processor;

import java.io.Serializable;

public class LockOwner implements Serializable {

  static final long serialVersionUID = 1L;
  static int counter;

  private int id;

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
}
