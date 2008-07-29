package org.sapia.corus.naming;

import javax.naming.Context;
import javax.naming.NamingException;

import org.sapia.corus.CorusRuntime;
import org.sapia.corus.ModuleHelper;
import org.sapia.corus.ServerStartedEvent;
import org.sapia.corus.cluster.ClusterManager;
import org.sapia.corus.event.EventDispatcher;
import org.sapia.soto.Env;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.rmi.naming.remote.ClientListener;
import org.sapia.ubik.rmi.naming.remote.JNDIServerHelper;
import org.sapia.ubik.rmi.naming.remote.RemoteContext;


/**
 * @author Yanick
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class JndiModuleImpl extends ModuleHelper implements JndiModule, Interceptor{
  private Context _context;
  private ClientListener _listener;
  
  /**
   * @see org.sapia.soto.EnvAware#setEnv(org.sapia.soto.Env)
   */
  public void setEnv(Env env) {
    _env = env;
  }
  
  /**
   * @see org.sapia.soto.Service#init()
   */
  public void init() throws Exception {
    EventChannel ec = ((ClusterManager)env().lookup(ClusterManager.ROLE)).getEventChannel();
    ((EventDispatcher)env().lookup(EventDispatcher.ROLE)).addInterceptor(ServerStartedEvent.class, this);
    _context = JNDIServerHelper.newRootContext(ec);
  }
  
  /**
   * @see org.sapia.soto.Service#dispose()
   */
  public void dispose() {
    try{
     _context.close();
    }catch(NamingException e){}
    
  }
  
  /*////////////////////////////////////////////////////////////////////
                          Module INTERFACE METHODS
  ////////////////////////////////////////////////////////////////////*/
  
  /**
   * @see org.sapia.corus.Module#getRoleName()
   */
  public String getRoleName() {
    return JndiModule.ROLE;
  }
  
  /*////////////////////////////////////////////////////////////////////
                        JndiModule INTERFACE METHODS
  ////////////////////////////////////////////////////////////////////*/
  
  /**
   * @see JndiModule#getContext()
   */
  public Context getContext() {
    return _context;
  }
  
  /**
   * This method is called once the corus server in which this instance
   * lives has started listening to requests.
   *
   * @param evt a <code>ServerStartedEvent</code> instance.
   */
  public void onServerStartedEvent(ServerStartedEvent evt){
    try{
      EventChannel ec = ((ClusterManager)env().lookup(ClusterManager.ROLE)).getEventChannel();
      CorusRuntime.getTransport().exportObject(_context);
      _listener = JNDIServerHelper.createClientListener(ec, CorusRuntime.getTransport().getServerAddress());
      /*_listener = new CorusClientListener(ec, CorusRuntime.getTransport().getServerAddress());
      ec.registerAsyncListener(JNDIServerHelper.JNDI_CLIENT_PUBLISH,  _listener);
      ec.dispatch(JNDIServerHelper.JNDI_SERVER_PUBLISH, CorusRuntime.getTransport().getServerAddress());*/
    }catch(Exception e){
      logger().error("Could not initialize client listener properly in JNDI module", e);
    }
  }
  
  public RemoteContext getRemoteContext(){
    return (RemoteContext)_context;
  }
  
  /*
  static final class CorusClientListener extends ClientListener{
    
    CorusClientListener(EventChannel channel, ServerAddress addr){
      super(channel, addr);
    }
    
    public void onAsyncEvent(RemoteEvent evt){
      super.onAsyncEvent(evt);
    }
  }*/
  
}
