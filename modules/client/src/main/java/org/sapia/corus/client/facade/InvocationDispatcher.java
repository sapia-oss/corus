package org.sapia.corus.client.facade;

import java.lang.reflect.Method;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;

/**
 * Abstracts the details of remote/clustered method on Corus server instances.
 * 
 * @author yduchesne
 * 
 */
public interface InvocationDispatcher {

  /**
   * This method dispatches the invocation to all Corus instances that are
   * targeted, and the result returned by each will be aggregated to the given
   * {@link Results} instance.
   * 
   * @param results
   *          the {@link Results} into which invocation results should be
   *          aggregated.
   * @param moduleInterface
   *          the {@link Class} corresponding to the interface on which to
   *          perform the invocation.
   * @param method
   *          the {@link Method} to invoke.
   * @param params
   *          the method's parameters.
   * @param cluster
   *          a {@link ClusterInfo} indicating which Corus servers should be
   *          targeted.
   * @throws Throwable
   *           a {@link Throwable} instance.
   */
  public <T, M> void invoke(Results<T> results, Class<M> moduleInterface, Method method, Object[] params, ClusterInfo cluster) throws Throwable;

  /**
   * This method will dispatch the invocation to all Corus instances that are
   * targeted, but only the result of the method invoked on the Corus server to
   * which the client is connected will be returned.
   * 
   * @param returnType
   *          the type of the invoked method's return value.
   * @param moduleInterface
   *          the {@link Class} corresponding to the interface on which to
   *          perform the invocation.
   * @param method
   *          the {@link Method} to invoke.
   * @param params
   *          the method's parameters.
   * @param cluster
   *          a {@link ClusterInfo} indicating which Corus servers should be
   *          targeted.
   * @return the method's return value.
   * @throws Throwable
   */
  public <T, M> T invoke(Class<T> returnType, Class<M> moduleInterface, Method method, Object[] params, ClusterInfo cluster) throws Throwable;

}
