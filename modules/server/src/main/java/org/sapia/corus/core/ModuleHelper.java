package org.sapia.corus.core;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.Module;
import org.sapia.corus.client.services.Service;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * This class can conveniently be extended by {@link Module} implementations.
 * 
 * @author Yanick Duchesne
 */
public abstract class ModuleHelper implements ApplicationContextAware, Service, Module, InitializingBean, DisposableBean{
  
  protected Logger  					 log = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());
  protected ApplicationContext appContext;
  @Autowired
  protected ServerContext 		 serverContext;

  public ModuleHelper() {
    super();
  }
  
  @Override
  public void afterPropertiesSet() throws Exception {
    this.init();
  }
  
  @Override
  public void destroy() throws Exception {
    this.dispose();
  }

  @Override
  public void setApplicationContext(ApplicationContext appCtx)
      throws BeansException {
    appContext = appCtx;
  }
    
  public void start() throws Exception {}

  public Logger logger(){
    return log;  
  }

  public ApplicationContext env(){
    return appContext;
  }
  
  protected ServerContext serverContext(){
    return serverContext;
  }
  
  protected InternalServiceContext services(){
    return serverContext.getServices();
  }
  
  protected <S> S lookup(Class<S> serviceInterface){
    return serverContext.lookup(serviceInterface);
  }

}
