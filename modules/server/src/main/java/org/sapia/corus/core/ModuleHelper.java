package org.sapia.corus.core;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.Module;
import org.sapia.corus.client.annotations.Bind;
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
  
  protected Logger  _logger = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());

  protected ApplicationContext _appContext;
  
  @Autowired
  protected ServerContext _serverContext;

  public ModuleHelper() {
    super();
  }
  
  @Override
  public void afterPropertiesSet() throws Exception {
    if(getClass().isAnnotationPresent(Bind.class)){
      Bind bind = getClass().getAnnotation(Bind.class);
      for(Class<?> moduleInterface : bind.moduleInterface()){
        _logger.debug(String.format("Binding %s as module %s", getClass().getName(), moduleInterface.getName()));
        _serverContext.getServices().bind(moduleInterface, this);
      }
    }    
    this.init();
  }
  
  @Override
  public void destroy() throws Exception {
    this.dispose();
  }

  @Override
  public void setApplicationContext(ApplicationContext appCtx)
      throws BeansException {
    _appContext = appCtx;
  }
    
  public void start() throws Exception {}

  public Logger logger(){
    return _logger;  
  }

  public ApplicationContext env(){
    return _appContext;
  }
  
  protected ServerContext serverContext(){
    return _serverContext;
  }
  
  protected InternalServiceContext services(){
    return _serverContext.getServices();
  }
  
  protected <S> S lookup(Class<S> serviceInterface){
    return _serverContext.lookup(serviceInterface);
  }

}
