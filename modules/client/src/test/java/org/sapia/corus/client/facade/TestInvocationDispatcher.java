package org.sapia.corus.client.facade;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.ubik.net.ServerAddress;

public class TestInvocationDispatcher implements InvocationDispatcher {

  private Map<Class<?>, Object> modules = new HashMap<Class<?>, Object>();
  private ServerAddress addr;

  public TestInvocationDispatcher(ServerAddress addr) {
    this.addr = addr;
  }

  public TestInvocationDispatcher add(Class<?> moduleInterface, Object instance) {
    modules.put(moduleInterface, instance);
    return this;
  }

  @Override
  public <T, M> T invoke(Class<T> returnType, Class<M> moduleInterface, Method method, Object[] params, ClusterInfo info) throws Throwable {
    M module = moduleInterface.cast(modules.get(moduleInterface));
    if (module == null) {
      throw new IllegalStateException(String.format("No module found for: %s", moduleInterface));
    }
    try {
      return returnType.cast(method.invoke(module, params));
    } catch (InvocationTargetException e) {
      throw e.getTargetException();
    }
  }

  @Override
  public <T, M> void invoke(Results<T> results, Class<M> moduleInterface, Method method, Object[] params, ClusterInfo cluster) throws Throwable {
    M module = moduleInterface.cast(modules.get(moduleInterface));
    if (module == null) {
      throw new IllegalStateException(String.format("No module found for: %s", moduleInterface));
    }

    try {
      T result = (T) method.invoke(module, params);
      results.incrementInvocationCount();
      results.addResult(new Result<T>(addr, result));
    } catch (Throwable e) {
    }
  }

}
