package org.sapia.corus.client.common.reference;

import java.io.Externalizable;

public interface Reference<T> extends Externalizable {

  public T get();

  public void set(T instance);
  
  public boolean setIf(T newState, T expectedCurrentState);

}