package org.sapia.corus.client.facade;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
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

import org.apache.http.conn.util.InetAddressUtils;
import org.sapia.corus.client.ClientDebug;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Corus;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.Results.ResultListener;
import org.sapia.corus.client.cli.ClientFileSystem;
import org.sapia.corus.client.common.Matcheable;
import org.sapia.corus.client.common.Matcheable.Pattern;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.exceptions.cli.ConnectionException;
import org.sapia.corus.client.facade.impl.ClientSideClusterInterceptor;
import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CurrentAuditInfo;
import org.sapia.corus.client.services.cluster.CurrentAuditInfo.AuditInfoRegistration;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.NoSuchObjectException;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.invocation.ClientPreInvokeEvent;
import org.sapia.ubik.rmi.server.transport.http.HttpAddress;
import org.sapia.ubik.util.Assertions;

/**
 * An instance of this class encapsulates objects pertaining to the connection
 * to a Corus server.
 * 
 * @author yduchesne
 * 
 */
public class CorusConnectionContextImpl implements CorusConnectionContext {

  private static final ClientDebug DEBUG = ClientDebug.get(CorusConnectionContextImpl.class);
  
  static final int  INVOKER_THREADS     = 10;
  static final long RECONNECT_INTERVAL  = 15000;

  private long                          lastReconnect     = System.currentTimeMillis();
  private Corus                         corus;
  private ServerAddress                 connectAddress;
  private CorusHost                     serverHost;
  private String                        domain;
  private Map<Class<?>, Object>         modules           = Collections.synchronizedMap(new HashMap<Class<?>, Object>());
  private Map<ServerAddress, CorusHost> otherHosts        = Collections.synchronizedMap(new HashMap<ServerAddress, CorusHost>());
  private Map<ServerAddress, Corus>     cachedStubs       = Collections.synchronizedMap(new HashMap<ServerAddress, Corus>());
  private Stack<ServerAddress>          connectionHistory = new Stack<ServerAddress>();
  private ExecutorService               executor;
  private ClientSideClusterInterceptor  interceptor;
  private ClientFileSystem              fileSys;
  private Stack<Pattern>                resultFilter      = new Stack<Matcheable.Pattern>();
  private HostSelectionContext          hostSelection     = new HostSelectionContextImpl();
  
  {
    resultFilter.push(Matcheable.AnyPattern.newInstance()); 
  }

  /**
   * @param host
   *          the host of the Corus server to connect to.
   * @param port
   *          the port of the Corus server to connect to.
   * @param fileSys
   *          the {@link ClientFileSystem}.
   * @param invokerThreads
   *          the number of threads to use when dispatching clustered method
   *          calls to targeted Corus instances.
   * @throws Exception
   *           if a problem occurs when attempting to connect to the Corus
   *           server.
   * @throws ConnectionException
   *           if not Corus server exists at the given host/port, or if a
   *           network-related problem occurs while attempting to connect to the
   *           given host/port.
   */
  public CorusConnectionContextImpl(String host, int port, ClientFileSystem fileSys, int invokerThreads) throws ConnectionException, Exception {
    connect(host, port);
    interceptor = new ClientSideClusterInterceptor();
    this.fileSys = fileSys;
    Hub.getModules().getClientRuntime().addInterceptor(ClientPreInvokeEvent.class, interceptor);
    executor = Executors.newFixedThreadPool(invokerThreads);
  }

  public CorusConnectionContextImpl(String host, int port, ClientFileSystem fileSys) throws Exception {
    this(host, port, fileSys, INVOKER_THREADS);
  }

  @Override
  public String getVersion() {
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
  public void setResultFilter(Pattern pattern) {
    resultFilter.push(pattern);
  }
  
  @Override
  public Pattern getResultFilter() {
    return resultFilter.peek();
  }
  
  @Override
  public void unsetResultFilter() {
    if (resultFilter.size() > 1) {
      resultFilter.pop();
    }
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
  public synchronized void connect(String host, int port) {
    if (InetAddressUtils.isIPv4Address(host)) {
      connectAddress = HttpAddress.newDefaultInstance(host, port);
    } else if (InetAddressUtils.isIPv6Address(host)) {
      connectAddress = HttpAddress.newDefaultInstance(host, port);
    } else {
      try {
        InetAddress addr = InetAddress.getByName(host);
        connectAddress = HttpAddress.newDefaultInstance(addr.getHostAddress(), port);
      } catch (java.net.UnknownHostException e) {
        throw new IllegalArgumentException("Unkown host: " + host, e);
      }
    }
    reconnect();
  }
  
  @Override
  public void connect(CorusHost host) {
    TCPAddress addr = host.getEndpoint().getServerTcpAddress();
    connect(addr.getHost(), addr.getPort());
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
      CurrentAuditInfo.unset();

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
  public <T, M> void invoke(Results<T> results, Class<M> moduleInterface, Method method, Object[] params, ClusterInfo cluster) throws Throwable {
    refresh();
    doInvoke(results, moduleInterface, method, params, cluster.convertLocalHost(this));
  }
    
  @Override
  public <T, M> T invoke(Class<T> returnType, Class<M> moduleInterface, Method method, Object[] params, ClusterInfo info) throws Throwable {
    return doInvoke(returnType, moduleInterface, method, params, info.convertLocalHost(this));
  }
   
  @SuppressWarnings(value = "unchecked")
  private <T, M> void doInvoke(Results<T> results, Class<M> moduleInterface, Method method, Object[] params, ClusterInfo cluster) throws Throwable {
    final List<Result<T>> resultList = new ArrayList<Result<T>>();
    results.addListener(new ResultListener<T>() {
      @Override
      public void onResult(Result<T> result) {
        resultList.add(result.filter(getResultFilter()));
      }
    });
    FacadeInvocationContext.set(resultList);

    try {
      if (cluster.isClustered()) {
        applyToCluster(results, moduleInterface, method, params, cluster);
      } else {
        T returnValue = (T) method.invoke(lookup(moduleInterface), params);
        results.incrementInvocationCount();
        Result<T> newResult = new Result<T>(serverHost, returnValue, Result.Type.forClass(method.getReturnType())).filter(getResultFilter());
        if (!getResultFilter().getClass().equals(Matcheable.AnyPattern.class) && newResult.size() == 0) {
          // noop
        } else {
          results.addResult(newResult);
        }
      }
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    }
  }
  
  private <T, M> T doInvoke(Class<T> returnType, Class<M> moduleInterface, Method method, Object[] params, ClusterInfo info) throws Throwable {
    try {
      ClientSideClusterInterceptor.clusterCurrentThread(info);

      Object toReturn = null;
      try {
        toReturn = method.invoke(lookup(moduleInterface), params);
      } catch (NoSuchObjectException e) {
        this.reconnect();
        toReturn = method.invoke(lookup(moduleInterface), params);
      }
      if (toReturn != null) {
        FacadeInvocationContext.set(toReturn);
        return returnType.cast(toReturn);
      } else {
        return null;
      }
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    } finally {
      ClientSideClusterInterceptor.unregister();
    }
  }

  @SuppressWarnings(value = "unchecked")
  void applyToCluster(final Results<?> results, final Class<?> moduleInterface, final Method method, final Object[] params, final ClusterInfo cluster) {

    List<CorusHost> hostList = new ArrayList<CorusHost>();
    if (cluster.isTargetingAllHosts()) {
      DEBUG.trace("Invocation targeting all hosts - %s", method.getName());
      hostList.add(serverHost);
      for (CorusHost otherHost : otherHosts.values()) {
        hostList.add(otherHost);
      }
    } else {
      DEBUG.trace("Invocation targeting specific hosts - %s", method.getName());
      for (ServerAddress t : cluster.getTargets()) {
        if (serverHost.getEndpoint().getServerAddress().equals(t)) {
          hostList.add(serverHost);
        } else {
          for (CorusHost o : otherHosts.values()) {
            if (o.getEndpoint().getServerAddress().equals(t)) {
              hostList.add(o);
            }
          }
        }
      }
    }
    
    if (DEBUG.enabled()) {
      DEBUG.trace("Targeting following hosts for %s", method.getName());
      for (CorusHost h : hostList) {
        DEBUG.trace("  --> %s", h.getFormattedAddress());
      }
    }
    
    final OptionalValue<AuditInfoRegistration> auditInfo = CurrentAuditInfo.get();

    Iterator<CorusHost> itr = hostList.iterator();
    List<Runnable> invokers = new ArrayList<Runnable>(hostList.size());
    while (itr.hasNext()) {
      final CorusHost currentHost = itr.next();

      Runnable invoker = new Runnable() {
        private Object module;
        private Object returnValue;
        private CorusHost host = currentHost;
        @SuppressWarnings("rawtypes")
        @Override
        public void run() {
          
          DEBUG.trace("Invoking %s on %s", method.getName(), host.getFormattedAddress());
          
          Corus       corus      = (Corus) cachedStubs.get(host.getEndpoint().getServerAddress());
          Result.Type resultType = Result.Type.forClass(method.getReturnType());

          if (corus == null) {
            try {
              corus = (Corus) Hub.connect(host.getEndpoint().getServerAddress());
              cachedStubs.put(host.getEndpoint().getServerAddress(), corus);
            } catch (java.rmi.RemoteException e) {
              DEBUG.trace("Adding error from %", host.getFormattedAddress());
              Result errorResult = new Result(host, e, resultType);
              results.addResult(errorResult);
              return;
            }
          }

          try {
            module = corus.lookup(moduleInterface.getName());
            if (auditInfo.isSet()) {
              DEBUG.trace("AuditInfo was set, tranferring to invoker thread for %s - invocation: %s", host.getFormattedAddress(), method.getName());
              CurrentAuditInfo.set(auditInfo.get(), host);
            }
            returnValue = method.invoke(module, params);
            DEBUG.trace("Invocation of %s completed for host %s", method.getName(), host.getFormattedAddress());
            
            Result newResult = new Result(host, returnValue, resultType).filter(getResultFilter());
            if (!getResultFilter().getClass().equals(Matcheable.AnyPattern.class) && newResult.size() == 0) {
              DEBUG.trace("Ignoring result from %s (%s elements were returned)", host.getFormattedAddress(), newResult.size());
              results.decrementInvocationCount();
            } else {
              DEBUG.trace("Adding result from %s (%s elements were returned)", host.getFormattedAddress(), newResult.size());
              results.addResult(newResult);
            }
          } catch (Exception err) {
            DEBUG.trace("Adding error from invocation on %", host.getFormattedAddress());
            Result errorResult = new Result(host, err, resultType);
            results.addResult(errorResult);
          } finally {
            if (auditInfo.isSet()) {
              CurrentAuditInfo.unset();
            }
          }

        }
      };
      invokers.add(invoker);
      results.incrementInvocationCount();
    }

    for (Runnable invoker : invokers) {
      executor.execute(invoker);
    }
  }
  
  @Override
  public HostSelectionContext getSelectedHosts() {
    return hostSelection;
  }

  @Override
  public Stack<ServerAddress> getConnectionHistory() {
    return connectionHistory;
  }

  @Override
  public synchronized <T> T lookup(Class<T> moduleInterface) {
    Object module = modules.get(moduleInterface);
    if (module == null) {
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
