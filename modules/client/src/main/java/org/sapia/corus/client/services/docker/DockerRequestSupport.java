package org.sapia.corus.client.services.docker;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.services.audit.AuditInfo;

/**
 * Base class for all Docker requests.
 * 
 * @author yduchesne
 *
 */
public abstract class DockerRequestSupport implements Externalizable {

  private AuditInfo auditInfo;
  
  /**
   * DO NOT CALL: meant for externalization only.
   */
  public DockerRequestSupport() {
  }
  
  protected DockerRequestSupport(AuditInfo auditInfo) {
    this.auditInfo = auditInfo;
  }
  
  public AuditInfo getAuditInfo() {
    return auditInfo;
  }
  
  // --------------------------------------------------------------------------
  // Externalizable interface
  
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    auditInfo = (AuditInfo) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(auditInfo);
  }
}
