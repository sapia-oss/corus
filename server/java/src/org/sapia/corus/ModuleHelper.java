package org.sapia.corus;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.admin.Module;
import org.sapia.soto.Env;
import org.sapia.soto.EnvAware;
import org.sapia.soto.Service;

/**
 * @author Yanick Duchesne
 */
public abstract class ModuleHelper implements Service, EnvAware, Module{
  
  protected Logger  _log = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());
  protected Env     _env;
  protected ServerContext _serverContext;

  public ModuleHelper() {
    super();
  }

  /**
   * @see org.sapia.soto.EnvAware#setEnv(org.sapia.soto.Env)
   */
  public void setEnv(Env env) {
    _env = env;
    _serverContext = InitContext.get().getServerContext();
    preInit();
  }
  
  public void preInit(){}

  /**
   * @see org.sapia.soto.Service#start()
   */
  public void start() throws Exception {}

  public Logger logger(){
    return _log;  
  }

  public Env env(){
    return _env;
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
