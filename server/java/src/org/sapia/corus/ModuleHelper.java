package org.sapia.corus;

import org.apache.log.Logger;
import org.apache.log.Hierarchy;
import org.sapia.soto.Env;
import org.sapia.soto.EnvAware;
import org.sapia.soto.Service;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public abstract class ModuleHelper implements Service, EnvAware, Module{
  
  protected Logger  _log = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());
  protected Env     _env;

  public ModuleHelper() {
    super();
  }

  /**
   * @see org.sapia.soto.EnvAware#setEnv(org.sapia.soto.Env)
   */
  public void setEnv(Env env) {
    _env = env;
  }

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

}
