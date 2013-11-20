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
public abstract class ModuleHelper implements ApplicationContextAware, Service, Module, InitializingBean, DisposableBean {

  protected Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());

  protected ApplicationContext appContext;

  @Autowired
  protected ServerContext serverContext;

  public ModuleHelper() {
    super();
  }

  // --------------------------------------------------------------------------
  // Setters provided for testing purposes.

  public void setServerContext(ServerContext serverContext) {
    this.serverContext = serverContext;
  }

  // --------------------------------------------------------------------------
  // ApplicationContextAware

  @Override
  public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
    appContext = appCtx;
  }

  // --------------------------------------------------------------------------
  // Lifecyle

  @Override
  public void afterPropertiesSet() throws Exception {
    this.init();
  }

  @Override
  public void destroy() throws Exception {
    this.dispose();
  }

  public void start() throws Exception {
  }

  // --------------------------------------------------------------------------
  // protected methods (to be used by inheriting classes)

  protected Logger logger() {
    return log;
  }

  protected ApplicationContext env() {
    return appContext;
  }

  // --------------------------------------------------------------------------
  // protected methods (to be used by inheriting classes)

  protected ServerContext serverContext() {
    return serverContext;
  }

  protected InternalServiceContext services() {
    return serverContext.getServices();
  }

  protected <S> S lookup(Class<S> serviceInterface) {
    return serverContext.lookup(serviceInterface);
  }

}
