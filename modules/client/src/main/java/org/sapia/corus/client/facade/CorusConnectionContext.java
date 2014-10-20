package org.sapia.corus.client.facade;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Stack;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Corus;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.ClientFileSystem;
import org.sapia.corus.client.common.Matcheable.Pattern;
import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.ubik.net.ServerAddress;

/**
 * Abstracts Corus connection behavior.
 * 
 * @author yduchesne
 * 
 */
public interface CorusConnectionContext {

  /**
   * @return the Corus server's version.
   */
  public String getVersion();

  /**
   * @return the {@link ClientFileSystem}.
   */
  public ClientFileSystem getFileSystem();

  /**
   * @return the domain/cluster of the Corus server to which this instance is
   *         connected.
   */
  public String getDomain();

  /**
   * @return the {@link ServerAddress} of the other Corus instances in the
   *         cluster.
   */
  public Collection<CorusHost> getOtherHosts();

  /**
   * @return the remote {@link Corus} instance, corresponding to the server's
   *         interface.
   */
  public Corus getCorus();

  /**
   * @return the {@link CorusHost} instance corresponding to the server to which
   *         this instance is connected.
   */
  public CorusHost getServerHost();

  /**
   * @return the {@link ServerAddress} of the Corus server to which this
   *         instance is connected.
   */
  public ServerAddress getAddress();

  /**
   * Reconnects to the corus server at the given host/port.
   * 
   * @param host
   *          the host of the server to reconnect to.
   * @param port
   *          the port of the server to reconnect to.
   * @throws CorusException
   */
  public void reconnect(String host, int port);

  /**
   * Reconnects to the corus server that this instance corresponds to.
   * 
   * @throws CorusException
   */
  public void reconnect();

  /**
   * Disconnects this instance, releases its resources. This instance should not
   * be used thereafter.
   */
  public void disconnect();

  /**
   * Indicates that the current thread should be clustered (or not), based on
   * the passed in {@link ClusterInfo}.
   * 
   * @param info
   *          a {@link ClusterInfo}.
   */
  public void clusterCurrentThread(ClusterInfo info);

  /**
   * @param results
   *          the {@link Results} instance to which to add the results acquired
   *          from the different Corus nodes.
   * @param moduleInterface
   *          the interface of the Corus module to on which to invoke a method.
   * @param method
   *          the {@link Method} to invoke.
   * @param params
   *          the method parameters.
   * @param cluster
   *          a {@link ClusterInfo}.
   * @throws Throwable
   *           if an error occurs performing the method invocation.
   */
  public <T, M> void invoke(Results<T> results, Class<M> moduleInterface, Method method, Object[] params, ClusterInfo cluster) throws Throwable;

  /**
   * @param returnType
   *          the {@link Class} corresponding to the type of object that is
   *          returned.
   * @param moduleInterface
   *          the interface of the Corus module to on which to invoke a method.
   * @param method
   *          the {@link Method} to invoke.
   * @param params
   *          the method parameters.
   * @param cluster
   *          a {@link ClusterInfo}.
   * @throws Throwable
   *           if an error occurs performing the method invocation.
   */
  public <T, M> T invoke(Class<T> returnType, Class<M> moduleInterface, Method method, Object[] params, ClusterInfo info) throws Throwable;

  /**
   * @return the {@link Stack} of addresses corresponding to the CLI's
   *         connection history.
   */
  public Stack<ServerAddress> getConnectionHistory();

  /**
   * @param moduleInterface
   *          the interface of the Corus module to lookup.
   * @return the remote module instance.
   */
  public <T> T lookup(Class<T> moduleInterface);

  /**
   * @param addr
   *          a {@link ServerAddress}.
   * @return the {@link CorusHost} corresponding to the given address.
   * @throws IllegalArgumentException
   *           if no such address is found.
   */
  public CorusHost resolve(ServerAddress addr) throws IllegalArgumentException;
  
  /**
   * @param pattern a {@link Pattern} to use for filtering results.
   */
  public void setResultFilter(Pattern pattern);
  
  /**
   * @return the {@link Pattern} corresponding to the currently set result filter.
   */
  public Pattern getResultFilter();
  
  /**
   * Resets the result filter to the default pattern (which, in fact, will not perform any filtering).
   */
  public void unsetResultFilter();

}