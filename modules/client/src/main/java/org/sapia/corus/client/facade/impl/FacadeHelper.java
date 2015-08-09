package org.sapia.corus.client.facade.impl;

import org.sapia.corus.client.facade.CorusConnectionContext;

public class FacadeHelper<M> {

  protected CorusConnectionContext context;
  protected ClusterInvoker<M> invoker;
  protected M proxy;

  public FacadeHelper(CorusConnectionContext context, Class<M> moduleInterface) {
    this.context = context;
    invoker = new ClusterInvoker<M>(moduleInterface, context);
    proxy = invoker.proxy(moduleInterface);
  }
  
  public CorusConnectionContext getContext() {
    return context;
  }

}
