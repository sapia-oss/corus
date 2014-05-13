package org.sapia.corus.cluster;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.services.Service;
import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.cluster.ClusterNotification;
import org.sapia.corus.client.services.cluster.ClusterStatus;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.corus.util.PropertiesFilter;
import org.sapia.corus.util.PropertiesUtil;
import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.EventChannelStateListener;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.Response;
import org.sapia.ubik.mcast.TimeoutException;
import org.sapia.ubik.net.ConnectionStateListener;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Remote;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.transport.IncomingCommandEvent;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Func;

/**
 * Implements the {@link ClusterManager} module.
 * 
 * @author Yanick Duchesne
 */
@Bind(moduleInterface = ClusterManager.class)
@Remote(interfaces = ClusterManager.class)
public class ClusterManagerImpl extends ModuleHelper implements ClusterManager, AsyncEventListener, EventChannelStateListener {

  private static final int START_UP_DELAY            = 15000;
  private static final int RECONNECTION_DELAY        = 10000;
  private static final int RECONNECTION_DELAY_OFFSET = 2000;

  private EventChannel channel;
  private Set<CorusHost> hostsInfos = Collections.synchronizedSet(new HashSet<CorusHost>());
  private Map<String, CorusHost> hostsByNode = Collections.synchronizedMap(new HashMap<String, CorusHost>());
  private ServerSideClusterInterceptor interceptor;
  private DeferredAsyncListener deferredListeners = new DeferredAsyncListener();
  private long startTime = System.currentTimeMillis();

  /**
   * @see Service#init()
   */
  public void init() throws Exception {
    channel = serverContext().getEventChannel();

    channel.registerAsyncListener(CorusPubEvent.class.getName(), deferredListeners.add(CorusPubEvent.class.getName(), this));
    channel.registerAsyncListener(CorusDiscoEvent.class.getName(), deferredListeners.add(CorusDiscoEvent.class.getName(), this));
    channel.addEventChannelStateListener(deferredListeners.add(this));
    
    channel.addConnectionStateListener(new ConnectionStateListener() {
        @Override
        public void onReconnected() {
          
          try {
            log.warn("Reconnection to event channel detected");
            Thread.sleep(new Random().nextInt(RECONNECTION_DELAY) + RECONNECTION_DELAY_OFFSET);
            publish();
          } catch (InterruptedException e) {
            log.debug("Thread interrupted: exiting");
          }
        }
        
        @Override
        public void onDisconnected() {
          log.warn("Connection from event channel dropped: will attempt reconnecting");
        }
        
        @Override
        public void onConnected() {
        }
      });
  }
  
  @Override
  public void start() throws Exception {
    super.start();
    logger().info("Starting event channel");
    channel.start();    
    interceptor = new ServerSideClusterInterceptor(log, serverContext());
    Hub.getModules().getServerRuntime().addInterceptor(IncomingCommandEvent.class, interceptor);
    deferredListeners.ready();
    publish();
  }
  
  private void publish() {
    if (log.isInfoEnabled()) {
      log.info("Signaling presence to cluster:");
      Properties mcastProperties = PropertiesUtil.filter(System.getProperties(),
          PropertiesFilter.NameContainsPropertiesFilter.createInstance("mcast"));
      for (String name : mcastProperties.stringPropertyNames()) {
        log.info(name + "=" + mcastProperties.getProperty(name));
      }
    }
    try {
      channel.dispatch(CorusPubEvent.class.getName(), new CorusPubEvent(serverContext().getCorusHost()));
    } catch (IOException e) {
      log.error("Error caught trying to signal presence to cluster", e);
    }    
  }

  /**
   * @see Service#dispose()
   */
  public void dispose() {
    channel.close();
  }

  // --------------------------------------------------------------------------
  // Module INTERFACE METHOD

  /**
   * @see org.sapia.corus.client.Module#getRoleName()
   */
  public String getRoleName() {
    return ClusterManager.ROLE;
  }

  // --------------------------------------------------------------------------
  // ClusterManager and instance METHODS

  @Override
  public synchronized Set<ServerAddress> getHostAddresses() {
    return Collects.convertAsSet(this.hostsInfos, new Func<ServerAddress, CorusHost>() {
      @Override
      public ServerAddress call(CorusHost arg) {
        return arg.getEndpoint().getServerAddress();
      }
    });
  }

  @Override
  public synchronized Set<CorusHost> getHosts() {
    return new HashSet<CorusHost>(hostsInfos);
  }

  @Override
  public ClusterStatus getClusterStatus() {
    return new ClusterStatus(channel.getRole(), this.serverContext.getCorus().getHostInfo());
  }

  @Override
  public void resync() {
    channel.resync();
    channel.forceResync();
  }

  @Override
  public EventChannel getEventChannel() {
    return channel;
  }

  @Override
  public Response send(ClusterNotification notif) throws IOException, TimeoutException {
    notif.addVisited(serverContext().getCorusHost().getEndpoint());
    if (notif.getTargets().isEmpty()) {
      logger().debug("No more target to send notification to: " + notif);
      return new Response(0, null);
    } else {
      logger().debug("Got remaining targets: " + notif.getTargets());
      Endpoint target = notif.getTargets().iterator().next();
      logger().debug("Sending to: " + target);
      return channel.send(target.getChannelAddress(), notif.getEventType(), notif);
    }
  }

  @Override
  public void onAsyncEvent(RemoteEvent remote) {
    Object event = null;

    try {
      event = remote.getData();
    } catch (IOException e) {
      log.debug("Could not get event data", e);

      return;
    }

    // ------------------------------------------------------------------------
    // publish event

    if (event instanceof CorusPubEvent) {
      CorusPubEvent evt = (CorusPubEvent) event;

      if (hostsInfos.add(evt.getOrigin())) {
        log.info(String.format("Corus discovered at %s", evt.getOrigin().getEndpoint().getServerAddress()));
        hostsInfos.add(evt.getOrigin());
      } else {
        log.info(String.format("Corus discovered at %s; already registered (that node probably was restarted): ", evt.getOrigin().getEndpoint()
            .getServerAddress()));
      }

      log.debug(String.format("Current addresses: %s", hostsInfos));

      hostsByNode.put(remote.getNode(), evt.getOrigin());

      try {
        channel.dispatch(remote.getUnicastAddress(), CorusDiscoEvent.class.getName(), new CorusDiscoEvent(serverContext().getCorusHost()));
      } catch (IOException e) {
        log.debug("Event channel could not dispatch event", e);
      }

      // ------------------------------------------------------------------------
      // discovery event

    } else if (event instanceof CorusDiscoEvent) {
      CorusDiscoEvent evt = (CorusDiscoEvent) event;
      if (hostsInfos.add(evt.getOrigin())) {
        log.debug(String.format("Existing corus discovered: %s", evt.getOrigin().getEndpoint().getServerAddress()));
        hostsInfos.add(evt.getOrigin());
      }
      log.debug(String.format("Current addresses: %s", hostsInfos));
      hostsByNode.put(remote.getNode(), evt.getOrigin());
    }
  }

  @Override
  public synchronized void onDown(final EventChannelEvent event) {
    synchronized (hostsByNode) {
      CorusHost host = hostsByNode.remove(event.getNode());
      if (host != null) {
        log.info(String.format("Corus server detected as down: %s. Removing from cluster view", host.getEndpoint()));
        synchronized (hostsInfos) {
          hostsInfos.remove(host);
        }
      }
    }
  }

  @Override
  public void onUp(EventChannelEvent event) {
    if (!hostsByNode.containsKey(event.getNode())) {
      try {
        // we want to make sure we're not sending a pub event prior to the
        // discovery process triggered at
        // startup having completed.
        if (System.currentTimeMillis() - startTime >= START_UP_DELAY) {
          log.debug("Corus appeared in cluster but not yet in host list; signaling presence to trigger discovery process");
          channel.dispatch(event.getAddress(), CorusPubEvent.class.getName(), new CorusPubEvent(serverContext().getCorusHost()));
        }
      } catch (IOException e) {
        log.error("Error sending publish event", e);
      }
    }
  }

}
