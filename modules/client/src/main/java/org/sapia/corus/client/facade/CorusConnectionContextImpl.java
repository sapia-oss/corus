package org.sapia.corus.client.facade;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Corus;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.Results.ResultListener;
import org.sapia.corus.client.cli.ClientFileSystem;
import org.sapia.corus.client.exceptions.cli.ConnectionException;
import org.sapia.corus.client.facade.impl.ClientSideClusterInterceptor;
import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.invocation.ClientPreInvokeEvent;
import org.sapia.ubik.rmi.server.transport.http.HttpAddress;
import org.sapia.ubik.util.Assertions;

/**
 * An instance of this class encapsulates objects pertaining to the connection to a
 * Corus server.
 * 
 * @author yduchesne
 *
 */
public class CorusConnectionContextImpl implements CorusConnectionContext {

  static final int INVOKER_THREADS 		 = 10;
  static final long RECONNECT_INTERVAL = 15000;
  
  private long 												  lastReconnect = System.currentTimeMillis();
  private Corus 											  corus;
  private ServerAddress 					      connectAddress;
  private CorusHost 									  serverHost;
  private String 										    domain;
  private Map<Class<?>, Object> 		    modules 		      = Collections.synchronizedMap(new HashMap<Class<?>, Object>());
  private Map<ServerAddress, CorusHost> otherHosts        = Collections.synchronizedMap(new HashMap<ServerAddress, CorusHost>());
  private Map<ServerAddress, Corus> 	  cachedStubs       = Collections.synchronizedMap(new HashMap<ServerAddress, Corus>());
  private Stack<ServerAddress>          connectionHistory = new Stack<ServerAddress>();
  private ExecutorService 						  executor;
  private ClientSideClusterInterceptor  interceptor;
  private ClientFileSystem              fileSys;

  /**
   * @param host the host of the Corus server to connect to.
   * @param port the port of the Corus server to connect to.
   * @param fileSys the {@link ClientFileSystem}.
   * @param invokerThreads the number of threads to use when dispatching clustered method calls
   * to targeted Corus instances.
   * @throws Exception if a problem occurs when attempting to connect to the Corus server.
   * @throws ConnectionException if not Corus server exists at the given host/port, or if a network-related
   * problem occurs while attempting to connect to the given host/port.
   */
  public CorusConnectionContextImpl(String host, int port, ClientFileSystem fileSys, int invokerThreads) throws ConnectionException, Exception {
    reconnect(host, port);
    interceptor = new ClientSideClusterInterceptor();
    this.fileSys = fileSys;
    Hub.getModules().getClientRuntime().addInterceptor(ClientPreInvokeEvent.class, interceptor);
    executor = Executors.newFixedThreadPool(invokerThreads);
  }

  public CorusConnectionContextImpl(String host, int port, ClientFileSystem fileSys) throws Exception {
    this(host, port, fileSys, INVOKER_THREADS);
  }

  @Override
  public String getVersion(){
    refresh();
    return corus.getVersion();
  }
  
  @Override
  public ClientFileSystem getFileSystem() {
    return fileSys;
  }
  
  @Override
  public String getDomain() {
    return domain;
  }

  @Override
  public Collection<CorusHost> getOtherHosts() {
    refresh();
    return Collections.unmodifiableCollection(otherHosts.values());
  }
  
  @Override
  public CorusHost resolve(ServerAddress addr) throws IllegalArgumentException {
    if (addr.equals(serverHost.getEndpoint().getServerAddress())) {
      return serverHost;
    }
    CorusHost toReturn = otherHosts.get(addr);
    Assertions.notNull(toReturn, "Could not resolve address: %s", addr);
    return toReturn;
  }
  
  @Override
  public Corus getCorus() {
    refresh();
    return corus;
  }

  @Override
  public CorusHost getServerHost() {
    return serverHost;
  }
  
  @Override
  public ServerAddress getAddress() {
    if (serverHost != null) {
      return serverHost.getEndpoint().getServerAddress();
    } else {
      return connectAddress;
    }
  }
  
  @Override
  public synchronized void reconnect(String host, int port) {
    connectAddress = HttpAddress.newDefaultInstance(host, port);
    reconnect();
  }

  @Override
  public synchronized void reconnect() {
    try {
      corus = (Corus) Hub.connect(connectAddress);
      domain = corus.getDomain();
      serverHost = corus.getHostInfo();
      otherHosts.clear();
      cachedStubs.clear();
      modules.clear();

      ClusterManager mgr = (ClusterManager) corus.lookup(ClusterManager.ROLE);
      for (CorusHost host : mgr.getHosts()) {
        otherHosts.put(host.getEndpoint().getServerAddress(), host);
      }
    } catch (RemoteException e) {
      throw new ConnectionException("Could not reconnect to Corus server", e);
    }
  }
  
  @Override
  public void disconnect() {
    this.executor.shutdownNow();
  }

  @Override
  public void clusterCurrentThread(ClusterInfo info) {
    refresh();
    ClientSideClusterInterceptor.clusterCurrentThread(info);
  }

  @Override
  @SuppressWarnings(value = "unchecked")
  public <T,M> void invoke(Results<T> results, Class<M> moduleInterface, Method method,
      Object[] params, ClusterInfo cluster) throws Throwable {
    refresh();
    
    final List<Result<T>> resultList = new ArrayList<Result<T>>();
    results.addListener(new ResultListener<T>() {
       @Override
      public void onResult(Result<T> result) {
         resultList.add(result);
      }
    });
    FacadeInvocationContext.set(resultList);
    
    try {
      if (cluster.isClustered()) {
        applyToCluster(results, moduleInterface, method, params, cluster);
      }
      else{
        T returnValue = (T) method.invoke(lookup(moduleInterface), params);
        results.incrementInvocationCount();
        results.addResult(new Result<T>(connectAddress, returnValue));
      }
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    } 
  }
  
  @Override
  public <T,M> T invoke(Class<T> returnType, Class<M> moduleInterface, Method method, Object[] params, ClusterInfo info) throws Throwable{
    try {
      
      ClientSideClusterInterceptor.clusterCurrentThread(info);
      
      Object toReturn = method.invoke(lookup(moduleInterface), params);
      if(toReturn != null){
        FacadeInvocationContext.set(toReturn);
        return returnType.cast(toReturn);
      } else {
        return null;
      }
    } catch(InvocationTargetException e) {
      throw e.getTargetException();
    } finally {
      ClientSideClusterInterceptor.unregister();
    }
  }

  @SuppressWarnings(value = "unchecked")
  void applyToCluster(
      final Results<?> results, 
      final Class<?> moduleInterface,
      final Method method, 
      final Object[] params,
      final ClusterInfo cluster) {
    
    List<ServerAddress> hostList = new ArrayList<ServerAddress>();
    if (cluster.isTargetingAllHosts()) {
      hostList.add(connectAddress);
      for (CorusHost otherHost: otherHosts.values()) {
        hostList.add(otherHost.getEndpoint().getServerAddress());
      }
    } else {
      hostList.addAll(cluster.getTargets());
    }
    
    Iterator<ServerAddress> itr = hostList.iterator();
    List<Runnable> invokers = new ArrayList<Runnable>(hostList.size());
    while (itr.hasNext()) {
      final ServerAddress addr = itr.next();
      
      Runnable invoker = new Runnable(){
        Object module;
        Object returnValue;

        @SuppressWarnings("rawtypes")
        @Override
        public void run() {
          Corus corus = (Corus) cachedStubs.get(addr);

          if (corus == null) {
            try {
              corus = (Corus) Hub.connect(addr);
              cachedStubs.put(addr, corus);
            } catch (java.rmi.RemoteException e) {
              results.decrementInvocationCount();
              return;
            }
          }

          try {
            module = corus.lookup(moduleInterface.getName());
            returnValue = method.invoke(module, params);
            results.addResult(new Result(addr, returnValue));
          } catch (Exception err) {
            results.decrementInvocationCount();
          } 

        }
      };
      invokers.add(invoker);
      results.incrementInvocationCount();
    }
    
    for(Runnable invoker:invokers){
      executor.execute(invoker);
    }
  }
  
  @Override
  public Stack<ServerAddress> getConnectionHistory() {
    return connectionHistory;
  }
  
  @Override
  public synchronized <T> T lookup(Class<T> moduleInterface){
    Object module  = modules.get(moduleInterface);
    if(module == null){
      module = corus.lookup(moduleInterface.getName());
      modules.put(moduleInterface, module);
    }
    return moduleInterface.cast(module);
  }

  protected void refresh() throws ConnectionException {
    if ((System.currentTimeMillis() - lastReconnect) > RECONNECT_INTERVAL) {
      reconnect();
      lastReconnect = System.currentTimeMillis();
    }
  }
}
