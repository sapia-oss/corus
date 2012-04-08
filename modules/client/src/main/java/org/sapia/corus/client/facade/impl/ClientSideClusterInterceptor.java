package org.sapia.corus.client.facade.impl;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.services.cluster.ClusteredCommand;
import org.sapia.corus.client.services.cluster.ClusteredInvoker;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.rmi.server.invocation.ClientPreInvokeEvent;


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

  public void onClientPreInvokeEvent(ClientPreInvokeEvent evt) {
    if (isCurrentThreadClustered()) {
      evt.setCommand(
          new ClusteredCommand(
              evt.getCommand(), 
              new ClusteredInvoker(), 
              registration.get().getTargets()));
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
