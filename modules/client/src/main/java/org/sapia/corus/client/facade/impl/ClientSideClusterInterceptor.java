package org.sapia.corus.client.facade.impl;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.services.cluster.ClusteredCommand;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.rmi.server.invocation.ClientPreInvokeEvent;
import org.sapia.ubik.util.Assertions;


/**
 * An instance of this class is used on the client side by facade implementation
 * in order to seamlessly cluster remote method calls.
 * 
 * @author Yanick Duchesne
 */
public class ClientSideClusterInterceptor implements Interceptor {
  
  private static ThreadLocal<ClusterInfo> registration = new ThreadLocal<ClusterInfo>();

  public static void clusterCurrentThread(ClusterInfo cluster) {
    registration.set(cluster);
  }
  
  public static void unregister() {
    registration.set(null);
  }

  public void onClientPreInvokeEvent(ClientPreInvokeEvent evt) {
    if (isCurrentThreadClustered()) {
      ClusteredCommand command = new ClusteredCommand(evt.getCommand());
      ClusterInfo      cluster = registration.get();
      Assertions.illegalState(cluster == null, "ClusterInfo instance not set");
      command.addTargets(cluster.getTargets());
      evt.setCommand(command);
    } 
  }

  private static boolean isCurrentThreadClustered() {
		ClusterInfo clustered = registration.get();

    if (clustered == null) {
      return false;
    } else {
      return clustered.isClustered();
    }
  }
}
