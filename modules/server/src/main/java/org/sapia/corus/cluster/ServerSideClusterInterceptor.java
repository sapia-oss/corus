package org.sapia.corus.cluster;

import org.apache.log.Logger;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Module;
import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.cluster.ClusteredCommand;
import org.sapia.corus.client.services.cluster.ClusteredInvoker;
import org.sapia.corus.core.ServerContext;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.rmi.replication.ReplicationEvent;
import org.sapia.ubik.rmi.server.invocation.ServerPreInvokeEvent;

/**
 * Handles clustered commands on the server-side.
 * 
 * @author Yanick Duchesne
 */
public class ServerSideClusterInterceptor implements Interceptor {
	
  private static ThreadLocal<ClusterInfo> registration = new ThreadLocal<ClusterInfo>();
  private Logger             log;
  private ClusterManager     cluster;
  private ServerContext      context;

  ServerSideClusterInterceptor(Logger log, ServerContext context) {
    this.log 		 = log;
    this.context = context;
    this.cluster = context.getServices().lookup(ClusterManager.class);
  }

  public static void clusterCurrentThread(ClusterInfo cluster) {
    registration.set(cluster);
  }
  
  public void onServerPreInvokeEvent(ServerPreInvokeEvent evt){
		if (evt.getInvokeCommand() instanceof ClusteredCommand) {
			ClusteredCommand cmd = (ClusteredCommand) evt.getInvokeCommand();

			if (evt.getTarget() instanceof Module) {
				try {
					ClusteredInvoker invoker = (ClusteredInvoker)cmd.getReplicatedInvoker();
					invoker.setUp(context.getCorus(), cluster);
					invoker.setModuleName(((Module)evt.getTarget()).getRoleName());
					  																									
				} catch (Throwable t) {
					log.error("Could not cluster command", t);
				}
			}
			else{
				cmd.disable();
			}
		}
  }

  public void onReplicationEvent(ReplicationEvent evt) {
    if (evt.getReplicatedCommand() instanceof ClusteredCommand) {
      ClusteredCommand cmd = (ClusteredCommand)evt.getReplicatedCommand();
      ClusteredInvoker invoker = (ClusteredInvoker)cmd.getReplicatedInvoker();
      invoker.setUp(context.getCorus(), cluster);
    }
  }

}
