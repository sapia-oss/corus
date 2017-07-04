package org.sapia.corus.client.facade.impl;

import org.sapia.corus.client.ClientDebug;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.common.encryption.Encryption;
import org.sapia.corus.client.services.cluster.ClusteredCommand;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CurrentAuditInfo;
import org.sapia.corus.client.services.cluster.CurrentAuditInfo.AuditInfoRegistration;
import org.sapia.corus.client.services.cluster.NonClusteredCommand;
import org.sapia.ubik.rmi.server.invocation.ClientPreInvokeEvent;
import org.sapia.ubik.util.Assertions;

/**
 * An instance of this class is used on the client side by facade implementation
 * in order to seamlessly cluster remote method calls.
 * 
 * @author Yanick Duchesne
 */
public class ClientSideClusterInterceptor {
  
  /**
   * Abstracts details of the client's connection.
   * 
   * @author yduchesne
   *
   */
  public interface ConnectionCallback {
    
    /**
     * @return the {@link CorusHost} to which the Corus client is currently connected.
     */
    public CorusHost getCurrentHost();
    
  }
  
  // ==========================================================================

  private static final ClientDebug              DEBUG        = ClientDebug.get(ClientSideClusterInterceptor.class);
  private static final ThreadLocal<ClusterInfo> REGISTRATION = new ThreadLocal<ClusterInfo>();
    
  public static void clusterCurrentThread(ClusterInfo cluster) {
    REGISTRATION.set(cluster);
  }

  public static void unregister() {
    REGISTRATION.set(null);
  }

  public void onClientPreInvokeEvent(ClientPreInvokeEvent evt) {
    if (isCurrentThreadClustered()) {
      DEBUG.trace("Performing command: %s", evt.getCommand().getMethodName());
      ClusteredCommand command = new ClusteredCommand(evt.getCommand());
      if (CurrentAuditInfo.isSet()) {
        AuditInfoRegistration reg = CurrentAuditInfo.get().get();
        command.setAuditInfo(
          reg.getAuditInfo().encryptWith(
            Encryption.getDefaultEncryptionContext(reg.getHost().getPublicKey())
          )
        );
      }
      ClusterInfo cluster = REGISTRATION.get();
      Assertions.illegalState(cluster == null, "ClusterInfo instance not set");
      command.exclude(cluster.getExcluded());
      command.addTargets(cluster.getTargets());
      evt.setCommand(command);
    } else {
      DEBUG.trace("Performing command: %s", evt.getCommand().getMethodName());
      NonClusteredCommand command = new NonClusteredCommand(evt.getCommand());
      if (CurrentAuditInfo.isSet()) {
        AuditInfoRegistration reg = CurrentAuditInfo.get().get();
        command.setAuditInfo(
          reg.getAuditInfo().encryptWith(
            Encryption.getDefaultEncryptionContext(reg.getHost().getPublicKey())
          )
        );
      }
      evt.setCommand(command);
    }
  }

  private static boolean isCurrentThreadClustered() {
    ClusterInfo clustered = REGISTRATION.get();

    if (clustered == null) {
      return false;
    } else {
      return clustered.isClustered();
    }
  }
}
