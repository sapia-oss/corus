package org.sapia.corus.cluster;

import org.apache.log.Logger;

import org.sapia.corus.ClusterInfo;
import org.sapia.corus.Module;

import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.rmi.replication.ReplicationEvent;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.invocation.ClientPreInvokeEvent;
import org.sapia.ubik.rmi.server.invocation.ServerPreInvokeEvent;


/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ClusterInterceptor implements Interceptor {
  private static ThreadLocal _registration = new ThreadLocal();
  private Logger             _log;

  public ClusterInterceptor() {
    Hub.clientRuntime.addInterceptor(ClientPreInvokeEvent.class, this);
  }

  ClusterInterceptor(Logger log) {
    _log = log;
  }

  public static void clusterCurrentThread(ClusterInfo cluster) {
    _registration.set(cluster);
  }

  public void onClientPreInvokeEvent(ClientPreInvokeEvent evt) {
    if (isCurrentThreadClustered()) {
      evt.setCommand(new ClusteredCommand(evt.getCommand(), 
                     new ClusteredInvoker(), 
                     ((ClusterInfo)_registration.get()).getTargets()));
    }
  }
  
  public void onServerPreInvokeEvent(ServerPreInvokeEvent evt){
		if (evt.getInvokeCommand() instanceof ClusteredCommand) {
			ClusteredCommand cmd = (ClusteredCommand) evt.getInvokeCommand();

			if (evt.getTarget() instanceof Module) {
				try {
					ClusteredInvoker invoker = (ClusteredInvoker)cmd.getReplicatedInvoker();
					invoker.setUp(ClusterManagerImpl.instance, _log);
					invoker.setModuleName(((Module)evt.getTarget()).getRoleName());
					  																									
				} catch (Throwable t) {
					_log.error("Could not cluster command", t);
				}
			}
			else{
				cmd.disable();
			}
		}
  }

  public void onReplicationEvent(ReplicationEvent evt) {
    if (evt.getReplicatedCommand() instanceof ClusteredCommand) {
    	ClusteredInvoker invoker = (ClusteredInvoker)((ClusteredCommand)evt.getReplicatedCommand()).getReplicatedInvoker();
     	invoker.setUp(ClusterManagerImpl.instance, _log);
    }
  }

  private static boolean isCurrentThreadClustered() {
		ClusterInfo clustered = (ClusterInfo) _registration.get();

    if (clustered == null) {
      return false;
    } else {
      return clustered.isClustered();
    }
  }
}
