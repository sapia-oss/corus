package org.sapia.corus.naming;

import javax.naming.Context;
import javax.naming.NamingException;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.services.Service;
import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.naming.JndiModule;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.corus.core.ServerStartedEvent;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.rmi.Remote;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.rmi.naming.remote.ClientListener;
import org.sapia.ubik.rmi.naming.remote.JNDIServerHelper;
import org.sapia.ubik.rmi.naming.remote.RemoteContext;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * This class implements a remote JNDI provider.
 * 
 * @author yduchesne
 */
@Bind(moduleInterface=JndiModule.class)
@Remote(interfaces=JndiModule.class)
public class JndiModuleImpl extends ModuleHelper implements JndiModule, Interceptor{

  @Autowired
  EventDispatcher _events;
  
  @Autowired
  ClusterManager _cluster;
  
  private Context _context;
  private ClientListener _listener;
  
  /**
   * @see Service#init()
   */
  public void init() throws Exception {
    EventChannel ec = _cluster.getEventChannel();
    _events.addInterceptor(ServerStartedEvent.class, this);
    _context = JNDIServerHelper.newRootContext(ec);
  }
  
  /**
   * @see Service#dispose()
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
   * @see org.sapia.corus.client.Module#getRoleName()
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
      EventChannel ec = _cluster.getEventChannel();
      serverContext().getTransport().exportObject(_context);
      _listener = JNDIServerHelper.createClientListener(ec, serverContext().getTransport().getServerAddress());
    }catch(Exception e){
      logger().error("Could not initialize client listener properly in JNDI module", e);
    }
  }
  
  public RemoteContext getRemoteContext(){
    return (RemoteContext)_context;
  }
  
}
