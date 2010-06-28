package org.sapia.corus.client.facade;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Corus;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.exceptions.cli.ConnectionException;
import org.sapia.corus.client.facade.impl.ClientSideClusterInterceptor;
import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.invocation.ClientPreInvokeEvent;

/**
 * An instance of this class encapsulates objects pertaining to the connection to a
 * Corus server.
 * 
 * @author yduchesne
 *
 */
public class CorusConnectionContext {

  static final int INVOKER_THREADS = 10;
  static final long RECONNECT_INTERVAL = 15000;
  private long _lastReconnect = System.currentTimeMillis();
  private Corus _corus;
  private ServerAddress _addr;
  private String _domain;
  private Map<Class<?>, Object> _modules = Collections.synchronizedMap(new HashMap<Class<?>, Object>());
  private Set<ServerAddress> _otherHosts = Collections.synchronizedSet(new HashSet<ServerAddress>());
  private Map<ServerAddress, Corus> _cachedStubs = Collections.synchronizedMap(new HashMap<ServerAddress, Corus>());
  private ExecutorService _executor;
  private ClientSideClusterInterceptor _interceptor;

  /**
   * @param host the host of the Corus server to connect to.
   * @param port the port of the Corus server to connect to.
   * @param invokerThreads the number of threads to use when dispatching clustered method calls
   * to targeted Corus instances.
   * @throws Exception if a problem occurs when attempting to connect to the Corus server.
   * @throws ConnectionException if not Corus server exists at the given host/port, or if a network-related
   * problem occurs while attempting to connect to the given host/port.
   */
  public CorusConnectionContext(String host, int port, int invokerThreads) throws ConnectionException, Exception {
    reconnect(host, port);
    _interceptor = new ClientSideClusterInterceptor();
    Hub.clientRuntime.addInterceptor(ClientPreInvokeEvent.class, _interceptor);
    _executor = Executors.newFixedThreadPool(invokerThreads);
  }

  public CorusConnectionContext(String host, int port) throws Exception {
    this(host, port, INVOKER_THREADS);
  }

  /**
   * @return the Corus server's version.
   */
  public String getVersion(){
    refresh();
    return _corus.getVersion();
  }

  /**
   * @return the domain/cluster of the Corus server to which this instance is
   *         connected.
   */
  public String getDomain() {
    return _domain;
  }

  /**
   * @return the {@link ServerAddress} of the other Corus instances in the
   *         cluster.
   */
  public Collection<ServerAddress> getOtherAddresses() {
    refresh();
    return Collections.unmodifiableCollection(_otherHosts);
  }

  /**
   * @return the remote {@link Corus} instance, corresponding to the server's
   *         interface.
   */
  public Corus getCorus() {
    refresh();
    return _corus;
  }

  /**
   * @return the {@link ServerAddress} of the Corus server to which this
   *         instance is connected.
   */
  public ServerAddress getAddress() {
    return _addr;
  }

  /**
   * Reconnects to the corus server at the given host/port.
   * 
   * @param host
   *          the host of the server to reconnect to.
   * @param port
   *          the port of the server to reconnect to.
   * @throws CorusException
   */
  public synchronized void reconnect(String host, int port) {
    _addr = new TCPAddress(host, port);
    reconnect();
  }

  /**
   * Reconnects to the corus server that this instance corresponds to.
   * 
   * @throws CorusException
   */
  public synchronized void reconnect() {
    try {
      _corus = (Corus) Hub.connect(((TCPAddress) _addr).getHost(),
          ((TCPAddress) _addr).getPort());
      _domain = _corus.getDomain();
      _otherHosts.clear();
      _cachedStubs.clear();
      _modules.clear();

      ClusterManager mgr = (ClusterManager) _corus.lookup(ClusterManager.ROLE);
      _otherHosts.addAll(mgr.getHostAddresses());
    } catch (RemoteException e) {
      throw new ConnectionException("Could not reconnect to Corus server", e);
    }
  }

  public void clusterCurrentThread(ClusterInfo info) {
    refresh();
    ClientSideClusterInterceptor.clusterCurrentThread(info);
  }

  @SuppressWarnings(value = "unchecked")
  public <T,M> void invoke(Results<T> results, Class<M> moduleInterface, Method method,
      Object[] params, ClusterInfo cluster) throws Throwable {
    refresh();

    try {
      Object returnValue = method.invoke(lookup(moduleInterface), params);
      results.addResult(new Result(_addr, returnValue));
    } catch (InvocationTargetException e) {
      //results.addResult(new Result(_addr, e.getTargetException()));
      throw e.getTargetException();
    }

    if (cluster.isClustered()) {
      //ClientSideClusterInterceptor.clusterCurrentThread(cluster);
      applyToCluster(results, moduleInterface, method, params, cluster);
    }
  }
  
  public <T,M> T invoke(Class<T> returnType, Class<M> moduleInterface, Method method, Object[] params, ClusterInfo info) throws Throwable{
    try{
      
      if(info.isClustered()){
        ClientSideClusterInterceptor.clusterCurrentThread(info);
      }
      
      Object toReturn = method.invoke(lookup(moduleInterface), params);
      if(toReturn != null){
        return returnType.cast(toReturn);
      }
      else{
        return null;
      }
    }catch(InvocationTargetException e){
      throw e.getTargetException();
    }
  }

  @SuppressWarnings(value = "unchecked")
  void applyToCluster(
      final Results results, 
      final Class moduleInterface,
      final Method method, 
      final Object[] params,
      final ClusterInfo cluster) {
    Runnable invoker = new Runnable() {
      public void run() {
        Set<ServerAddress> otherHosts;

        if (cluster.getTargets() != null) {
          otherHosts = new HashSet<ServerAddress>(_otherHosts);
          otherHosts.retainAll(cluster.getTargets());
        } else {
          otherHosts = _otherHosts;
        }
        Iterator<ServerAddress> itr = otherHosts.iterator();
        Corus corus;
        Object module;
        Object returnValue;

        while (itr.hasNext()) {
          TCPAddress addr = (TCPAddress)itr.next();
          corus = (Corus) _cachedStubs.get(addr);

          if (corus == null) {
            try {
              corus = (Corus) Hub.connect(addr.getHost(), addr.getPort());
              _cachedStubs.put(addr, corus);
            } catch (java.rmi.RemoteException e) {
              continue;
            }
          }

          module = corus.lookup(moduleInterface.getName());

          try {
            returnValue = method.invoke(module, params);
            results.addResult(new Result(addr, returnValue));
          } catch (Exception err) {
            // noop
          } 
          continue;
        }

        results.complete();
      }
    };
    _executor.execute(invoker);
  }
  
  public synchronized <T> T lookup(Class<T> moduleInterface){
    Object module  = _modules.get(moduleInterface);
    if(module == null){
      module = _corus.lookup(moduleInterface.getName());
      _modules.put(moduleInterface, module);
    }
    return moduleInterface.cast(module);
  }

  protected void refresh() throws ConnectionException {
    if ((System.currentTimeMillis() - _lastReconnect) > RECONNECT_INTERVAL) {
      reconnect();
      _lastReconnect = System.currentTimeMillis();
    }
  }
}
