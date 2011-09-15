package org.sapia.corus.cluster;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.services.Service;
import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.cluster.ServerHost;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Consts;
import org.sapia.ubik.rmi.replication.ReplicationEvent;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.invocation.ServerPreInvokeEvent;


/**
 * @author Yanick Duchesne
 */
@Bind(moduleInterface=ClusterManager.class)
public class ClusterManagerImpl extends ModuleHelper
  implements ClusterManager, AsyncEventListener {
  static ClusterManagerImpl instance;
  private String               _multicastAddress = Consts.DEFAULT_MCAST_ADDR;
  private int                  _multicastPort    = Consts.DEFAULT_MCAST_PORT;
  private EventChannel         _channel;
  private Set<ServerAddress>   _hostsAddresses   = Collections.synchronizedSet(new HashSet<ServerAddress>());
  private Set<ServerHost>      _hostsInfos       = Collections.synchronizedSet(new HashSet<ServerHost>());
  private ServerSideClusterInterceptor _interceptor;
  
  /**
   * @param addr this instance's multicast address.
   */
  public void setMcastAddress(String addr){
    _multicastAddress = addr;
  }
  
  /**
   * @param port this instance's multicast port.
   */
  public void setMcastPort(int port){
    _multicastPort = port;
  }

  /**
   * @see Service#init()
   */
  public void init() throws Exception {
    instance = this;
    _channel = new EventChannel(
        serverContext().getDomain(), 
        _multicastAddress,
        _multicastPort);
    _channel.registerAsyncListener(CorusPubEvent.class.getName(), this);
    _channel.start();
    _channel.setBufsize(4000);
    _logger.info("Signaling presence to cluster on: " + _multicastAddress + ":" + _multicastPort);
    _channel.dispatch(CorusPubEvent.class.getName(),
            new CorusPubEvent(true, serverContext().getServerAddress(), serverContext().getHostInfo()));
  }
  
  @Override
  public void start() throws Exception {
    super.start();
    _interceptor = new ServerSideClusterInterceptor(_logger, serverContext());
    Hub.serverRuntime.addInterceptor(ServerPreInvokeEvent.class, _interceptor);
    Hub.serverRuntime.addInterceptor(ReplicationEvent.class, _interceptor);    
  }
  
  /**
   * @see Service#dispose()
   */
  public void dispose() {
    _channel.close();
  }

  /*////////////////////////////////////////////////////////////////////
                        Module INTERFACE METHOD
  ////////////////////////////////////////////////////////////////////*/

  /**
   * @see org.sapia.corus.client.Module#getRoleName()
   */
  public String getRoleName() {
    return ClusterManager.ROLE;
  }

  /*////////////////////////////////////////////////////////////////////
                     ClusterManager and instance METHODS
  ////////////////////////////////////////////////////////////////////*/

  /**
   * @see ClusterManager#getHostAddresses()
   */
  public synchronized Set<ServerAddress> getHostAddresses() {
    return new HashSet<ServerAddress>(_hostsAddresses);
  }

  public synchronized Set<ServerHost> getHosts() {
    return new HashSet<ServerHost>(_hostsInfos);
  }

  /**
   * @see ClusterManager#getEventChannel()
   */
  public EventChannel getEventChannel() {
    return _channel;
  }

  /**
   * @see AsyncEventListener#onAsyncEvent(org.sapia.ubik.mcast.RemoteEvent)
   */
  public void onAsyncEvent(RemoteEvent remote) {
    Object event = null;

    try {
      event = remote.getData();
    } catch (IOException e) {
      _logger.debug("Could not get event data", e);

      return;
    }

    if (event instanceof CorusPubEvent) {
      CorusPubEvent evt  = (CorusPubEvent) event;
      ServerAddress  addr = evt.getOrigin();
      
      if(_hostsAddresses.add(evt.getOrigin())){
        _logger.debug(String.format("Corus discovered at %s" + addr));        
        _hostsInfos.add(evt.getHostInfo());
      }
      else{
        _logger.debug(String.format("Corus discovered at %s; already registered (that node probably was restarted): ", addr));
      }
      
      if (evt.isNew()) {
        try {
          if(remote.getUnicastAddress() == null){
            _channel.dispatch(
                CorusPubEvent.class.getName(),
                new CorusPubEvent(false, serverContext().getTransport().getServerAddress(), serverContext().getHostInfo())
            );
          }
          else{
            _channel.dispatch(
                remote.getUnicastAddress(),
                CorusPubEvent.class.getName(),
                new CorusPubEvent(false, serverContext().getTransport().getServerAddress(), serverContext().getHostInfo())
            );
          }
        } catch (IOException e) {
          _logger.debug("Event channel could not dispatch event", e);
        }
      } else {
        _logger.debug("Existing corus discovered: " + addr);
      }
    } 
  }
  
}
