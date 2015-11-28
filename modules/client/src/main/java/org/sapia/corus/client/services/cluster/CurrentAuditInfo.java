package org.sapia.corus.client.services.cluster;

import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.services.audit.AuditInfo;
import org.sapia.ubik.util.Assertions;

/**
 * Performs thread registration of {@link AuditInfo} data.
 * 
 * @author yduchesne
 *
 */
public class CurrentAuditInfo {
  
  public static class AuditInfoRegistration {
    
    private CorusHost host;
    private AuditInfo auditInfo;
    
    public AuditInfoRegistration(CorusHost host, AuditInfo auditInfo) {
      this.host      = host;
      this.auditInfo = auditInfo;
    }
    
    public AuditInfo getAuditInfo() {
      return auditInfo;
    }
    
    public CorusHost getHost() {
      return host;
    }
  }
  
  // ===========================================================================

  private static final ThreadLocal<OptionalValue<AuditInfoRegistration>> CURRENT_AUDIT_INFO = new ThreadLocal<OptionalValue<AuditInfoRegistration>>();
  
  private CurrentAuditInfo() {
  }
  
  public static boolean isSet() {
    return CURRENT_AUDIT_INFO.get() != null && CURRENT_AUDIT_INFO.get().isSet();
  }
  
  public static boolean isNull() {
    return CURRENT_AUDIT_INFO.get() == null || CURRENT_AUDIT_INFO.get().isNull();
  }
  
  public static void unset() {
    CURRENT_AUDIT_INFO.set(null);
  }
  
  public static OptionalValue<AuditInfoRegistration> get() {
    OptionalValue<AuditInfoRegistration> toReturn = CURRENT_AUDIT_INFO.get();
    if (toReturn == null) {
      toReturn = OptionalValue.none();
    }
    return toReturn;
  }
  
  /**
   * @param info the {@link AuditInfo} to use.
   * @param host the {@link CorusHost} to whicht he given audit info is targeted (and whose public key should be used for encryption).
   */
  public static void set(AuditInfo info, CorusHost host) {
    Assertions.illegalState(info.isEncrypted(), "AuditInfo must NOT be encrypted at this point");
    CURRENT_AUDIT_INFO.set(OptionalValue.of(new AuditInfoRegistration(host, info)));
  }
  
  public static void set(AuditInfoRegistration from, CorusHost newHost) {
    Assertions.illegalState(from.getAuditInfo().isEncrypted(), "AuditInfo must NOT be encrypted at this point");
    CURRENT_AUDIT_INFO.set(OptionalValue.of(new AuditInfoRegistration(newHost, from.getAuditInfo())));
  }
}
