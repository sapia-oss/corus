package org.sapia.corus.cluster;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sapia.corus.CorusRuntime;
import org.sapia.corus.ModuleHelper;
import org.sapia.corus.util.IntProperty;
import org.sapia.corus.util.Property;
import org.sapia.corus.util.StringProperty;
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
public class ClusterManagerImpl extends ModuleHelper
  implements ClusterManager, AsyncEventListener {
  static ClusterManagerImpl instance;
  private Property _multicastAddress = new StringProperty(Consts.DEFAULT_MCAST_ADDR);
  private Property _multicastPort    = new IntProperty(Consts.DEFAULT_MCAST_PORT);
  private EventChannel      _channel;
  private Map               _hostsById   = Collections.synchronizedMap(new HashMap());
  private Map               _hostsByAddr = Collections.synchronizedMap(new HashMap());
  
  /**
   * @param addr this instance's multicast address.
   */
  public void setMcastAddress(Property addr){
    _multicastAddress = addr;
  }
  
  /**
   * @param port this instance's multicast port.
   */
  public void setMcastPort(Property port){
    _multicastPort = new IntProperty(port.getIntValue());
  }

  /**
   * @see org.sapia.soto.Service#init()
   */
  public void init() throws Exception {
    instance = this;
    _channel = new EventChannel(
        CorusRuntime.getCorus().getDomain(), 
        _multicastAddress.getValue(),
        _multicastPort.getIntValue());
    _channel.registerAsyncListener(CorusPubEvent.class.getName(), this);
    _channel.start();
    _channel.setBufsize(4000);    
    _log.info("Signaling presence to cluster on: " + _multicastAddress + ":" + _multicastPort);
    _channel.dispatch(CorusPubEvent.class.getName(),
            new CorusPubEvent(true, CorusRuntime.getTransport().getServerAddress()));
		ClusterInterceptor interceptor = new ClusterInterceptor(_log);
		
    Hub.serverRuntime.addInterceptor(ServerPreInvokeEvent.class, interceptor);
		Hub.serverRuntime.addInterceptor(ReplicationEvent.class, interceptor);    
  }
  
  /**
   * @see org.sapia.soto.Service#dispose()
   */
  public void dispose() {
    _channel.close();
  }

  /*////////////////////////////////////////////////////////////////////
                        Module INTERFACE METHOD
  ////////////////////////////////////////////////////////////////////*/

  /**
   * @see org.sapia.corus.admin.Module#getRoleName()
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
  public synchronized Set getHostAddresses() {
    return new HashSet(_hostsById.keySet());
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
      _log.debug("Could not get event data", e);

      return;
    }

    if (event instanceof CorusPubEvent) {
      CorusPubEvent evt  = (CorusPubEvent) event;
      ServerAddress  addr = evt.getOrigin();
      _hostsByAddr.put(evt.getOrigin(), addr);
      _hostsById.put(addr, evt.getOrigin());

      if (evt.isNew()) {
        _log.debug("New corus discovered: " + addr);

        try {
          _channel.dispatch(CorusPubEvent.class.getName(),
                  new CorusPubEvent(false, CorusRuntime.getTransport().getServerAddress()));
        } catch (IOException e) {
          _log.debug("Event channel could not dispatch event", e);
        }
      } else {
        _log.debug("Existing corus discovered: " + addr);
      }
    } 
  }
}
