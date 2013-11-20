package org.sapia.corus.client.transport;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.Module;
import org.sapia.ubik.rmi.server.oid.OID;

/**
 * An {@link OID} implementation used for Corus {@link Module}s published as
 * remote objects.
 * 
 * @author yduchesne
 * 
 */
public class CorusModuleOID implements OID, Externalizable {

  private String moduleName;

  /**
   * Meant for externalization only.
   */
  public CorusModuleOID() {
  }

  /**
   * @param moduleName
   *          the name of the module to which this instance corresponds.
   */
  public CorusModuleOID(String moduleName) {
    this.moduleName = moduleName;
  }

  /**
   * @return this instance's corresponding module.
   */
  public String getModuleName() {
    return moduleName;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof CorusModuleOID) {
      CorusModuleOID other = (CorusModuleOID) obj;
      return moduleName.equals(other.moduleName);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return moduleName.hashCode();
  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    moduleName = in.readUTF();
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(moduleName);
  }

  @Override
  public String toString() {
    return moduleName;
  }

}
