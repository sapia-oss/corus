package org.sapia.corus.cluster;

import java.rmi.RemoteException;
import java.util.Set;

import org.apache.log.Logger;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.cluster.ClusteredCommand;
import org.sapia.corus.client.services.cluster.CorusCallback;
import org.sapia.corus.core.ServerContext;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.transport.Connections;
import org.sapia.ubik.rmi.server.transport.IncomingCommandEvent;
import org.sapia.ubik.rmi.server.transport.RmiConnection;

/**
 * Handles clustered commands on the server-side.
 * 
 * @author Yanick Duchesne
 */
public class ServerSideClusterInterceptor implements Interceptor, CorusCallback {
	
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
  
  public void onIncomingCommandEvent(IncomingCommandEvent evt) {
    if (evt.getCommand() instanceof ClusteredCommand) {
      ClusteredCommand cmd = (ClusteredCommand) evt.getCommand();
      if (log.isErrorEnabled()) {
        log.debug("Received clustered command " + cmd.getMethodName());
      }      
      cmd.setCallback(this);
    }
  }
  
  // --------------------------------------------------------------------------
  // CorusCallback interface
  
  @Override
  public void debug(String message) {
    log.debug(message);
  }
  
  @Override
  public boolean isDebug() {
    return log.isDebugEnabled();
  }
  
  
  @Override
  public void error(String message, Throwable err) {
    log.error(message, err);
  }
  
  @Override
  public ServerAddress getCorusAddress() {
    return context.getServerAddress();
  }
  
  @Override
  public Set<ServerAddress> getSiblings() {
    return cluster.getHostAddresses();
  }
  
  @Override
  public Object lookup(String moduleName) {
    return context.lookup(moduleName);
  }
  
  @Override
  public Object send(ClusteredCommand cmd, ServerAddress nextTarget)
      throws Exception {
    Connections    pool = Hub.getModules().getTransportManager().getConnectionsFor(nextTarget);
    RmiConnection  conn =  null;
    try {
      conn = pool.acquire();
      conn.send(cmd);
      Object returnValue = conn.receive();
      pool.release(conn);
      return returnValue;
    } catch (RemoteException re) {
      if(conn != null) {
        pool.invalidate(conn);
      }
      pool.clear();
      throw re;
    } catch (Exception e) {
      log.error("Problem sending clustered command to " + nextTarget, e);
      if(conn != null) {
        conn.close();
      }
      throw e;
    }   
    
  }

}
