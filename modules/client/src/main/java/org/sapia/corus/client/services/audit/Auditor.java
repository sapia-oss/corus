package org.sapia.corus.client.services.audit;

import org.sapia.corus.client.Module;
import org.sapia.ubik.net.ServerAddress;

/**
 * Specifies behavior for auditing.
 * 
 * @author yduchesne
 *
 */
public interface Auditor extends Module {
  
  public static final String ROLE = Auditor.class.getName();
  
  /**
   * @param info the {@link AuditInfo} to record audit information with.
   * @param remoteAddr the {@link ServerAddress} of the host from which the method invocation was received.
   * @param moduleName the name of the module on which a method call is being performed.
   * @param methodName the name of the method corresponding to the method call being performed.
   */
  public void audit(AuditInfo info, ServerAddress remoteAddr, String moduleName, String methodName);

}
