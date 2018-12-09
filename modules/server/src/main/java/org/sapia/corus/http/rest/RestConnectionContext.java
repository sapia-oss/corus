package org.sapia.corus.http.rest;

import java.io.InterruptedIOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ExecutorService;

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
import org.sapia.ubik.rmi.threads.Threads;
import org.sapia.ubik.util.Assertions;

import com.google.common.collect.Lists;

/**
 * An instance of this class encapsulates objects pertaining to the connection to a Corus server.
 * 
 * @author yduchesne
 * 
 */
public class RestConnectionContext implements CorusConnectionContext {

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
  private boolean                       lenient;

  {
    resultFilter.push(Matcheable.AnyPattern.newInstance()); 
  }
  
  /**
   * @param current
   *          the current {@link Corus} host.
   * @param fileSys
   *          the {@link ClientFileSystem}.
   * @param lenient if <code>true</code>, indicates that this instance is lenient to network failures when
   *                performing clustered commands.
   */
  public RestConnectionContext(Corus current, ClientFileSystem fileSys, boolean lenient) {
    this.corus         = current;
    this.serverHost  = corus.getHostInfo();
    this.domain      = corus.getDomain();
    this.interceptor = new ClientSideClusterInterceptor();
    this.fileSys     = fileSys;
    this.lenient     = lenient;
    Hub.getModules().getClientRuntime().addInterceptor(ClientPreInvokeEvent.class, interceptor);
    executor = Threads.createIoOutboundPool();
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
    executor.shutdown();
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
            toReturn = doSafellyInvokeOneNodeOfCluster(moduleInterface, method, params);
            
          }
          
        // only other hosts are targeted
        } else {
          Assertions.illegalState(getOtherHosts().isEmpty(), "No hosts to target command to");
          
          info.addExcluded(getAddress());
          ClientSideClusterInterceptor.clusterCurrentThread(info);
          if (log.isDebugEnabled()) {
            log.debug("Other host(s) targeted for: " + method + " (" + info + ")");
          }
          toReturn = doSafellyInvokeOneNodeOfCluster(moduleInterface, method, params);
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
  
  private <M> Object doSafellyInvokeOneNodeOfCluster(Class<M> moduleInterface, Method method, Object[] params) throws Throwable {
    LinkedList<CorusHost> hostList = Lists.newLinkedList(this.getOtherHosts());
    CorusHost nextHost = null;
    Corus     corus    = null;
    Object    result   = null;
    Boolean   invocationCompleted = false;
    
    while (hostList.size() > 0 && invocationCompleted == false) {
      try {
        nextHost = hostList.removeFirst();
        corus = getRemoteCorus(nextHost);
      } catch (Exception e) {
        if (lenient) {
          log.info(String.format("Network error connecting to host: %s. Lenient mode is enabled, error ignored", nextHost), e);
        } else {
          log.warn("Network error connecting host: " + nextHost, e);
          throw e;
        }
      }
      
      if (corus != null) {
        if (CurrentAuditInfo.isSet()) {
          AuditInfoRegistration reg = CurrentAuditInfo.get().get();
          CurrentAuditInfo.set(reg.getAuditInfo(), nextHost);
        } else {
          CurrentAuditInfo.set(AuditInfo.forCurrentUser(), nextHost);
        }
   
        try {
          log.info(String.format("Invoking rest endpoint (%s) for module %s", corus, moduleInterface.getName()));
          Object remoteModule = corus.lookup(moduleInterface.getName());
          result = method.invoke(remoteModule, params);
          invocationCompleted = true;
        } catch (InvocationTargetException | UndeclaredThrowableException e) {
          Throwable wrapped;
          if (e instanceof InvocationTargetException) {
            wrapped = ((InvocationTargetException) e).getTargetException();
          } else {
            wrapped = ((UndeclaredThrowableException) e).getUndeclaredThrowable();
          }
          
          if ((wrapped instanceof RemoteException) || (wrapped instanceof SocketException) || (wrapped instanceof InterruptedIOException)) {
            if (lenient) {
              log.debug(String.format("Network error invoking host: %s. Lenient mode is enabled, error ignored", nextHost), e);
            } else {
              log.error("Could not cluster method call: " + method, wrapped);
              throw wrapped;
            }
          }
        } catch (Exception e) {
          log.error("Could not cluster method call: " + method, e);
          throw e;
        }
      }
    }
    
    return result;
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
              if (lenient) {
                log.debug(String.format("Network error invoking host: %s. Lenient mode is enabled, error ignored", addr), e);
              } else {
                log.debug("Network error invoking host: " + addr, e);
                Result errorResult = new Result(addr, e, resultType);
                results.addResult(errorResult);
              }

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
          } catch (UndeclaredThrowableException e) {
            Throwable wrapped = e.getCause();
            if ((wrapped instanceof RemoteException) || (wrapped instanceof SocketException) || (wrapped instanceof InterruptedIOException)) {
              if (lenient) {
                log.debug(String.format("Network error invoking host: %s. Lenient mode is enabled, error ignored", addr), e);
              } else {
                log.debug("Network error invoking host: " + addr, e);
                Result errorResult = new Result(addr, e, resultType);
                results.addResult(errorResult);
              }
            } else {
              log.debug("Error invoking on host: " + addr, wrapped);
              Result errorResult = new Result(addr, wrapped, resultType);
              results.addResult(errorResult);
            }
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
  
  private Corus getRemoteCorus(CorusHost remoteHost) throws RemoteException {
    Corus corus = (Corus) cachedStubs.get(remoteHost.getEndpoint().getServerAddress());
    if (corus == null) {
      corus = (Corus) Hub.connect(remoteHost.getEndpoint().getServerAddress());
      cachedStubs.put(remoteHost.getEndpoint().getServerAddress(), corus);
    }
    return corus;
  }
  
  // --------------------------------------------------------------------------
  // Unimplemented
  
  @Override
  public void reconnect() {
  }

}
