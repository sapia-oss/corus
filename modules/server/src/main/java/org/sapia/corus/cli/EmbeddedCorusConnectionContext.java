package org.sapia.corus.cli;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Corus;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.Results.ResultListener;
import org.sapia.corus.client.cli.ClientFileSystem;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.ubik.net.ServerAddress;

/**
 * An embedded {@link CorusConnectionContext} which allows running the Corus CLI "in-VM".
 * 
 * @author yduchesne
 *
 */
public class EmbeddedCorusConnectionContext implements CorusConnectionContext {

  private static final Stack<ServerAddress> EMPTY_HISTORY = new Stack<ServerAddress>();

  private Corus            corus;
  private ClientFileSystem fileSys;

  /**
   * @param corus
   *          the local Corus instance.
   * @param fileSys
   *          the {@link ClientFileSystem}.
   */
  public EmbeddedCorusConnectionContext(Corus corus, ClientFileSystem fileSys) {
    this.corus   = corus;
    this.fileSys = fileSys;
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
  public String getDomain() {
    return corus.getDomain();
  }

  @Override
  public Collection<CorusHost> getOtherHosts() {
    return Collections.emptyList();
  }

  @Override
  public CorusHost resolve(ServerAddress addr) throws IllegalArgumentException {
    if (addr.equals(corus.getHostInfo().getEndpoint().getServerAddress())) {
      return corus.getHostInfo();
    }
    throw new IllegalArgumentException("Embedded context cannot resolve remote address");
  }

  @Override
  public Corus getCorus() {
    return corus;
  }

  @Override
  public CorusHost getServerHost() {
    return corus.getHostInfo();
  }

  @Override
  public ServerAddress getAddress() {
    return corus.getHostInfo().getEndpoint().getServerAddress();
  }

  @Override
  @SuppressWarnings(value = "unchecked")
  public <T, M> void invoke(Results<T> results, Class<M> moduleInterface, Method method, Object[] params, ClusterInfo cluster) throws Throwable {
    final List<Result<T>> resultList = new ArrayList<Result<T>>();
    results.addListener(new ResultListener<T>() {
      @Override
      public void onResult(Result<T> result) {
        resultList.add(result);
      }
    });

    try {
      T returnValue = (T) method.invoke(lookup(moduleInterface), params);
      results.incrementInvocationCount();
      results.addResult(new Result<T>(corus.getHostInfo(), returnValue, Result.Type.forClass(method.getReturnType())));
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    }
  }

  @Override
  public <T, M> T invoke(Class<T> returnType, Class<M> moduleInterface, Method method, Object[] params, ClusterInfo info) throws Throwable {
    try {
      Object toReturn = method.invoke(lookup(moduleInterface), params);
      if (toReturn != null) {
        return returnType.cast(toReturn);
      } else {
        return null;
      }
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    }
  }

  @Override
  public Stack<ServerAddress> getConnectionHistory() {
    return EMPTY_HISTORY;
  }

  @Override
  public synchronized <T> T lookup(Class<T> moduleInterface) {
    Object module = corus.lookup(moduleInterface.getName());
    return moduleInterface.cast(module);
  }
  
  // --------------------------------------------------------------------------
  
  @Override
  public synchronized void reconnect(String host, int port) {
  }

  @Override
  public synchronized void reconnect() {
  }

  @Override
  public void disconnect() {
  }

  @Override
  public void clusterCurrentThread(ClusterInfo info) {
  }
}
