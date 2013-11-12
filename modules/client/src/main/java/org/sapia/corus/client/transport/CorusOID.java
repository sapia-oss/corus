package org.sapia.corus.client.transport;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.Corus;
import org.sapia.ubik.rmi.server.oid.OID;

/**
 * An {@link OID} implementation used the {@link Corus} instance.
 * 
 * @author yduchesne
 *
 */
public class CorusOID implements OID, Externalizable {
  
  private static final String NAME = Corus.class.getName();

  /**
   * Meant for externalization only.
   */
  public CorusOID() {
  }
  
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CorusOID) {
      return true;
    } 
    return false;
  }
  
  @Override
  public int hashCode() {
    return NAME.hashCode();
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
  }
  
  @Override
  public String toString() {
    return NAME;
  }

}
