package org.sapia.corus.client.services.cluster;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.services.audit.AuditInfo;
import org.sapia.corus.client.services.audit.Auditor;
import org.sapia.corus.client.transport.CorusModuleOID;
import org.sapia.corus.client.transport.CorusOID;
import org.sapia.ubik.rmi.NoSuchObjectException;
import org.sapia.ubik.rmi.server.command.InvokeCommand;
import org.sapia.ubik.rmi.server.oid.OID;
import org.sapia.ubik.util.Assertions;

/**
 * Wraps an {@link InvokeCommand} and adds {@link AuditInfo} data.
 * 
 * @author yduchesne
 *
 */
public class NonClusteredCommand extends InvokeCommand implements CorusCallbackCapable {
  
  private OptionalValue<AuditInfo> auditInfo = OptionalValue.none();

  private transient CorusCallback callback;
  
  /**
   * DO NOT CALL: meant for externalization.
   */
  public NonClusteredCommand() {
    
  }
  
  public NonClusteredCommand(InvokeCommand cmd) {
    super(cmd.getOID(), cmd.getMethodName(), cmd.getParams(), cmd.getParameterTypes(), null);
  }
  
  @Override
  public void setCorusCallback(CorusCallback callback) {
    this.callback = callback;
  }
  
  public void setAuditInfo(AuditInfo auditInfo) {
    Assertions.illegalState(!auditInfo.isEncrypted(), "Expected AuditInfo to be encrypted at this point");
    this.auditInfo = OptionalValue.of(auditInfo);
  }

  /**
   * @return this instance's optional {@link AuditInfo}.
   */
  public OptionalValue<AuditInfo> getAuditInfo() {
    return auditInfo;
  }
  
  @Override
  protected Object doGetObjectFor(OID oid) throws NoSuchObjectException {
    if (oid instanceof CorusModuleOID) {
      return callback.getCorus().lookup(((CorusModuleOID) oid).getModuleName());
    } else if (oid instanceof CorusOID) {
      return callback.getCorus();
    }
    return super.doGetObjectFor(oid);
  }
  
  @Override
  public Object execute() throws Throwable {
    Assertions.illegalState(callback == null, "Corus callback not set");
    if (getOID() instanceof CorusModuleOID) {
      Auditor auditor = (Auditor) callback.getCorus().lookup(Auditor.ROLE);
      if (auditInfo.isSet()) {
        AuditInfo decrypted = auditInfo.get().decryptWith(callback.getDecryptionContext());
        auditor.audit(decrypted, getConnection().getServerAddress(), ((CorusModuleOID) getOID()).getModuleName(), getMethodName());
      }
    }
    return super.execute();
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    auditInfo = (OptionalValue<AuditInfo>) in.readObject();
    super.readExternal(in);
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(auditInfo);
    super.writeExternal(out);
  }
  
}
