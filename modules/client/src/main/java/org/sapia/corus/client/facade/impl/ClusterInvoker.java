package org.sapia.corus.client.facade.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.DefaultInvocationDispatcher;
import org.sapia.corus.client.facade.InvocationDispatcher;

public class ClusterInvoker<M>{
  
  private InvocationDispatcher dispatcher;
  private Class<M> moduleInterface;
  private Method toInvoke;
  private Object[] params;
  
  public ClusterInvoker(Class<M> moduleInterface, CorusConnectionContext context){
    this(moduleInterface, new DefaultInvocationDispatcher(context));
  }
  
  public ClusterInvoker(Class<M> moduleInterface, InvocationDispatcher dispatcher) {
    this.moduleInterface = moduleInterface;
    this.dispatcher = dispatcher;
  }

  /**
   * Performs a "lenient" invocation (only {@link RuntimeException}s will be throw; non-runtime exceptions will be wrapped
   * in runtime exceptions).
   * 
   * @param results a {@link Result}s instance that will be filled with the results of the clustered invocation.
   * @param info a {@link ClusterInfo} instance.
   * 
   * @see InvocationDispatcher#invoke(Results, Class, Method, Object[], ClusterInfo)
   */
  public synchronized <T> void invokeLenient(Results<T> results, ClusterInfo info){
    try{
      dispatcher.invoke(results, moduleInterface, toInvoke, params, info);
    }catch(RuntimeException e){
      throw e;
    }catch(Throwable e){
      throw new RuntimeException(e);
    }
  }

  /**
   * Performs an invocation, throwing an error if one occurs.
   * 
   * @param results a {@link Result}s instance that will be filled with the results of the clustered invocation.
   * @param info a {@link ClusterInfo} instance.
   * 
   * @see InvocationDispatcher#invoke(Results, Class, Method, Object[], ClusterInfo)
   */
  public synchronized <T> void invoke(Results<T> results, ClusterInfo info) throws Throwable{
    dispatcher.invoke(results, moduleInterface, toInvoke, params, info);
  }

  /**
   * Performs a "lenient" invocation (only {@link RuntimeException}s will be throw; non-runtime exceptions will be wrapped
   * in runtime exceptions).
   *
   * @param returnType the return type of the invoked method.
   * @param info a {@link ClusterInfo}
   * @return the invocation's return value.
   * 
   * @see InvocationDispatcher#invoke(Class, Class, Method, Object[], ClusterInfo)
   */
  public synchronized <T> T invokeLenient(Class<T> returnType, ClusterInfo info){
    try{
      return dispatcher.invoke(returnType, moduleInterface, toInvoke, params, info);
    }catch(RuntimeException e){
      throw e;
    }catch(Throwable e){
      throw new RuntimeException(e);
    }
  }

  /**
   * Performs an invocation, throwing an error if one occurs.
   * 
   * @param returnType the return type of the invoked method.
   * @param info a {@link ClusterInfo}
   * @return the invocation's return value.
   * 
   * @see InvocationDispatcher#invoke(Class, Class, Method, Object[], ClusterInfo)
   */
  public synchronized <T> T invoke(Class<T> returnType, ClusterInfo info) throws Throwable{
    return dispatcher.invoke(returnType, moduleInterface, toInvoke, params, info);
  }
 
  /**
   * @param moduleInterface the interface whose methods should be invoked.
   * @return a dynamic proxy implementing the given interface.
   */
  public <T> T proxy(Class<T> moduleInterface){
    return moduleInterface.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{moduleInterface}, new ClusterInvokerHandler()));
  }
  
  class ClusterInvokerHandler implements InvocationHandler{
    
    @Override
    public synchronized Object invoke(Object proxy, Method someMethod, Object[] someParams)
        throws Throwable {
      toInvoke = someMethod;
      params = someParams;
      return null;
    }
  }
}
