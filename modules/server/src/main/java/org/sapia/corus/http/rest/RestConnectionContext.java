package org.sapia.corus.http.rest;

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

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Corus;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.Results.ResultListener;
import org.sapia.corus.client.cli.ClientFileSystem;
import org.sapia.corus.client.common.Matcheable;
import org.sapia.corus.client.common.Matcheable.Pattern;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.HostSelectionContext;
import org.sapia.corus.client.facade.HostSelectionContextImpl;
import org.sapia.corus.client.facade.impl.ClientSideClusterInterceptor;
import org.sapia.corus.client.services.audit.AuditInfo;
import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CurrentAuditInfo;
import org.sapia.corus.client.services.cluster.CurrentAuditInfo.AuditInfoRegistration;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.invocation.ClientPreInvokeEvent;
import org.sapia.ubik.util.Assertions;

/**
 * An instance of this class encapsulates objects pertaining to the connection to a Corus server.
 * 
 * @author yduchesne
 * 
 */
public class RestConnectionContext implements CorusConnectionContext {

  static final int INVOKER_THREADS = 10;

  private Logger                        log          = Hierarchy.getDefaultHierarchy().getLoggerFor(RestConnectionContext.class.getName());
  private Corus                         corus;
  private CorusHost                     serverHost;
  private String                        domain;
  private Map<Class<?>, Object>         modules      = Collections.synchronizedMap(new HashMap<Class<?>, Object>());
  private Map<ServerAddress, Corus>     cachedStubs  = Collections.synchronizedMap(new HashMap<ServerAddress, Corus>());
  private ExecutorService               executor;
  private ClientSideClusterInterceptor  interceptor;
  private ClientFileSystem              fileSys;
  private Stack<Pattern>                resultFilter  = new Stack<Matcheable.Pattern>();
  private HostSelectionContext          hostSelection = new HostSelectionContextImpl();

  
  {
    resultFilter.push(Matcheable.AnyPattern.newInstance()); 
  }
  
  /**
   * @param current
   *          the current {@link Corus} host.
   * @param fileSys
   *          the {@link ClientFileSystem}.
   * @param invokerThreads
   *          the number of threads to use when dispatching clustered method
   *          calls to targeted Corus instances.
   */
  public RestConnectionContext(Corus current, ClientFileSystem fileSys, int invokerThreads) {
    this.corus         = current;
    this.serverHost  = corus.getHostInfo();
    this.domain      = corus.getDomain();
    this.interceptor = new ClientSideClusterInterceptor();
    this.fileSys     = fileSys;
    Hub.getModules().getClientRuntime().addInterceptor(ClientPreInvokeEvent.class, interceptor);
    executor = Executors.newFixedThreadPool(invokerThreads);
  }

  /**
   * Internally calls the {@link RestConnectionContext#RestConnectionContext(Corus, ClientFileSystem, int)}, passing
   * in the default number of invoker threads (see {@link #INVOKER_THREADS}).
   * 
   * @param current
   *          the current {@link Corus} host.
   * @param fileSys
   *          the {@link ClientFileSystem}.
   */
  public RestConnectionContext(Corus current, ClientFileSystem fileSys) {
    this(current, fileSys, INVOKER_THREADS);
  }
  
  // --------------------------------------------------------------------------
  // Basic stuff: domain, version, ClientFileSystem.

  @Override
  public String getDomain() {
    return domain;
  }
  
  @Override
  public String getVersion() {
    return corus.getVersion();
  }

  @Override
  public ClientFileSystem getFileSystem() {
    return fileSys;
  }

  @Override
  public Corus getCorus() {
    return corus;
  }

  @Override
  public CorusHost getServerHost() {
    return serverHost;
  }

  @Override
  public ServerAddress getAddress() {
    return serverHost.getEndpoint().getServerAddress();
  }  
  
  // --------------------------------------------------------------------------
  // Result filtering is not used for now
  
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
  
  // --------------------------------------------------------------------------
  // Core functionality

  @Override
  public void connect(String host, int port) {
    for (CorusHost h : getOtherHosts()) {
      TCPAddress addr = h.getEndpoint().getServerTcpAddress();
      if (addr.getHost().equals(host) && addr.getPort() == port) {
        this.serverHost = h;
      }
    }
    throw new IllegalArgumentException(String.format("No host found for host:port => %s:%s", host, port));
  }
  
  @Override
  public void connect(CorusHost host) {
    TCPAddress addr = host.getEndpoint().getServerTcpAddress();
    connect(addr.getHost(), addr.getPort());
  }
  
  @Override
  public Collection<CorusHost> getOtherHosts() {
    return ((ClusterManager) corus.lookup(ClusterManager.ROLE)).getHosts();
  }

  @Override
  public CorusHost resolve(ServerAddress addr) throws IllegalArgumentException {
    if (addr.equals(serverHost.getEndpoint().getServerAddress())) {
      return serverHost;
    }
    
    Collection<CorusHost> hosts = ((ClusterManager) corus.lookup(ClusterManager.ROLE)).getHosts();
    CorusHost toReturn = null;
    for (CorusHost h : hosts) {
      if (h.getEndpoint().getServerAddress().equals(addr)) {
        toReturn = h;
      }
    }
   
    Assertions.notNull(toReturn, "Could not resolve address: %s", addr);
    return toReturn;
  }

  /**
   * Empty implementation.
   */
  @Override
  public Stack<ServerAddress> getConnectionHistory() {
    return new Stack<ServerAddress>();
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
  
  @Override
  public void disconnect() {
    executor.shutdownNow();
  }
  
  @Override
  public HostSelectionContext getSelectedHosts() {
    return hostSelection;
  }

  @Override
  public void clusterCurrentThread(ClusterInfo info) {
    ClientSideClusterInterceptor.clusterCurrentThread(info);
  }

  @Override
  public <T, M> void invoke(Results<T> results, Class<M> moduleInterface, Method method, Object[] params, ClusterInfo cluster) throws Throwable {
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
        resultList.add(result);
      }
    });

    try {
      if (cluster.isClustered()) {
        applyToCluster(results, moduleInterface, method, params, cluster);
      } else {
        T returnValue = (T) method.invoke(lookup(moduleInterface), params);
        results.incrementInvocationCount();
        results.addResult(new Result<T>(serverHost, returnValue, Result.Type.forClass(method.getReturnType())));
      }
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    }
  }

  private <T, M> T doInvoke(Class<T> returnType, Class<M> moduleInterface, Method method, Object[] params, ClusterInfo info) throws Throwable {
    try {

      Object toReturn = null;
      
      if (info.isClustered()) {
        if (log.isDebugEnabled()) {
          log.debug("Invoking method " + method + " for cluster (" + info + ")");
        }
        
        if (info.isTargetingHost(getAddress())) {
          info.addExcluded(getAddress());
          if (log.isDebugEnabled()) {
            log.debug("Current host targeted for: " + method);
          }
          // invoking on this host first
          toReturn = method.invoke(lookup(moduleInterface), params);

          if (info.isTargetingAllHosts() && !getOtherHosts().isEmpty()) {
            if (log.isDebugEnabled()) {
              log.debug("Other host(s) also targeted for: " + method);
            }
            ClientSideClusterInterceptor.clusterCurrentThread(info);
          
            if (log.isDebugEnabled()) {
              log.debug("Chaining invocation to other hosts for: " + method);
            }
            CorusHost nextHost  = this.getOtherHosts().iterator().next();
            Corus     corus     = getRemoteCorus(nextHost);
            if (CurrentAuditInfo.isSet()) {
              AuditInfoRegistration reg = CurrentAuditInfo.get().get();
              CurrentAuditInfo.set(reg.getAuditInfo(), nextHost);
            } else {
              CurrentAuditInfo.set(AuditInfo.forCurrentUser(), nextHost);
            }
       
            Object remoteModule = corus.lookup(moduleInterface.getName());
            try {
              toReturn = method.invoke(remoteModule, params);
            } catch (InvocationTargetException e) {
              log.error("Could not cluster method call: " + method, e.getTargetException());
              throw e.getTargetException();
            } catch (Exception e) {
              log.error("Could not cluster method call: " + method, e);
              throw e;
            }
          }
          
        // only other hosts are targeted
        } else {
          Assertions.illegalState(getOtherHosts().isEmpty(), "No hosts to target command to");
          
          info.addExcluded(getAddress());
          ClientSideClusterInterceptor.clusterCurrentThread(info);
          if (log.isDebugEnabled()) {
            log.debug("Other host(s) targeted for: " + method + " (" + info + ")");
          }
          CorusHost nextHost = getOtherHosts().iterator().next();
          Corus     corus    = getRemoteCorus(nextHost);
          if (CurrentAuditInfo.isSet()) {
            AuditInfoRegistration reg = CurrentAuditInfo.get().get();
            CurrentAuditInfo.set(reg.getAuditInfo(), nextHost);
          } else {
            CurrentAuditInfo.set(AuditInfo.forCurrentUser(), nextHost);
          }
          
          Object remoteModule = corus.lookup(moduleInterface.getName());
          try {
            toReturn = method.invoke(remoteModule, params);
          } catch (InvocationTargetException e) {
            throw e.getTargetException();
          } catch (Exception e) {
            throw e;
          }
        } 
      } else {
        if (log.isDebugEnabled()) {
          log.debug("Invocation NOT clustered for: " + method);
        }
        toReturn = method.invoke(lookup(moduleInterface), params);
      }
      
      if (toReturn != null) {
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

    final OptionalValue<AuditInfoRegistration> auditInfo = CurrentAuditInfo.get();
    
    List<CorusHost> hostList = new ArrayList<CorusHost>();
    if (log.isDebugEnabled()) {
      log.debug("==> Dispatching method invocation to cluster: " + method);
    }
    if (cluster.isTargetingAllHosts()) {
      hostList.add(serverHost);
      for (CorusHost otherHost : getOtherHosts()) {
        hostList.add(otherHost);
      }
    } else {
      for (ServerAddress t : cluster.getTargets()) {
        if (serverHost.getEndpoint().getServerAddress().equals(t)) {
          hostList.add(serverHost);
        } else {
          for (CorusHost o : getOtherHosts()) {
            if (o.getEndpoint().getServerAddress().equals(t)) {
              hostList.add(o);
            }
          }
        }
        
      }
    }

    Iterator<CorusHost> itr = hostList.iterator();
    List<Runnable> invokers = new ArrayList<Runnable>(hostList.size());
    while (itr.hasNext()) {
      final CorusHost addr = itr.next();

      Runnable invoker = new Runnable() {
        Object module;
        Object returnValue;

        @SuppressWarnings("rawtypes")
        @Override
        public void run() {
          Corus       corus      = (Corus) cachedStubs.get(addr.getEndpoint().getServerAddress());
          Result.Type resultType = Result.Type.forClass(method.getReturnType());
          if (log.isDebugEnabled()) {
            log.debug("Invoking on host: " + addr);
          }
          if (corus == null) {
            try {
              corus = (Corus) Hub.connect(addr.getEndpoint().getServerAddress());
              cachedStubs.put(addr.getEndpoint().getServerAddress(), corus);
            } catch (java.rmi.RemoteException e) {
              log.debug("Error invoking on host: " + addr, e);
              Result errorResult = new Result(addr, e, resultType);
              results.addResult(errorResult);
              return;
            }
          }

          try {
            module = corus.lookup(moduleInterface.getName());
            if (auditInfo.isSet()) {
              CurrentAuditInfo.set(auditInfo.get(), addr);
            } else {
              CurrentAuditInfo.set(AuditInfo.forCurrentUser(), addr);
            }
            returnValue = method.invoke(module, params);
            results.addResult(new Result(addr, returnValue, resultType));
          } catch (Exception err) {
            log.debug("Error invoking on host: " + addr, err);
            Result errorResult = new Result(addr, err, resultType);
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
    
    if (log.isDebugEnabled()) {
      log.debug("==> Completed dispatching of method invocation to cluster: " + method);
    }
  }

  // --------------------------------------------------------------------------
  // Unimplemented
  
  @Override
  public void reconnect() {
  }
  
  private Corus getRemoteCorus(CorusHost remoteHost) throws RemoteException {
    Corus corus = (Corus) cachedStubs.get(remoteHost.getEndpoint().getServerAddress());
    if (corus == null) {
      corus = (Corus) Hub.connect(remoteHost.getEndpoint().getServerAddress());
      cachedStubs.put(remoteHost.getEndpoint().getServerAddress(), corus);
    }
    return corus;
  }

}
