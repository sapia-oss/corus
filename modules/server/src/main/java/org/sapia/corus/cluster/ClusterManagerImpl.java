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
import org.sapia.corus.core.InternalCorus;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.corus.taskmanager.core.BackgroundTaskConfig;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
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

  private static final int CLUSTER_STATE_CHECK_INTERVAL = 5000;
  private static final int START_UP_DELAY               = 15000;
  private static final int RECONNECTION_DELAY           = 10000;
  private static final int RECONNECTION_DELAY_OFFSET    = 2000;

  private EventChannel                  channel;
  //private Map<ServerAddress, CorusHost> hostsByAddress    = Collections.synchronizedMap(new HashMap<ServerAddress, CorusHost>());
  private Map<String, CorusHost>        hostsByNode       = Collections.synchronizedMap(new HashMap<String, CorusHost>());
  private ServerSideClusterInterceptor  interceptor;
  private DeferredAsyncListener         deferredListeners = new DeferredAsyncListener();
  private long                          startTime         = System.currentTimeMillis();

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

    BackgroundTaskConfig taskConf = BackgroundTaskConfig.create().setExecDelay(CLUSTER_STATE_CHECK_INTERVAL).setExecInterval(CLUSTER_STATE_CHECK_INTERVAL);
    serverContext().getServices().getTaskManager().executeBackground(new Task<Void, Void>() {
      @Override
      public Void execute(TaskExecutionContext ctx, Void param) throws Throwable {
        if (channel.getView().getNodeCount() != hostsByNode.size()) {
          logger().info("Detected cluster desynchronization, refreshing view");
          Set<String> channelNodes = new HashSet<>(channel.getView().getNodes());
          channelNodes.removeAll(hostsByNode.keySet());
          for (String channelNode : channelNodes) {
            try {              
              channel.send(channel.getView().getNodeInfo(channelNode).getAddr(), CorusPubEvent.class.getName(), new CorusPubEvent(serverContext().getCorusHost()));
            } catch (IOException e) {
              log.error("Error caught trying refresh cluster view", e);
            } 
          }
        }
        return null;
      }
    }, null,taskConf);
    
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
    return Collects.convertAsSet(hostsByNode.values(), new Func<ServerAddress, CorusHost>() {
      @Override
      public ServerAddress call(CorusHost host) {
        return host.getEndpoint().getServerAddress();
      }
    });
  }

  @Override
  public synchronized Set<CorusHost> getHosts() {
    return new HashSet<CorusHost>(hostsByNode.values());
  }
  
  @Override
  public CorusHost resolveHost(ServerAddress hostAddress)
      throws IllegalArgumentException {
    for (CorusHost h : hostsByNode.values()) {
      if (h.getEndpoint().getServerAddress().equals(hostAddress)) {
        return h;
      }
    }
    throw new IllegalArgumentException("Could not find Corus host information for provided address: " + hostAddress);
  }

  @Override
  public ClusterStatus getClusterStatus() {
    return new ClusterStatus(this.serverContext.getCorus().getHostInfo(), this.hostsByNode.size());
  }

  @Override
  public void resync() {
    channel.resync();
  }
  
  @Override
  public void changeCluster(String name) {
    log.info("Changing cluster to: " + name);
    channel.changeDomain(name);
    ((InternalCorus) serverContext().getCorus()).changeDomain(name);
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
      return new Response(channel.getUnicastAddress(), 0, null);
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
      addNode(evt.getOrigin());
      if (log.isDebugEnabled()) {
        log.debug(String.format("Current hosts: %s", hostsByNode.values()));
      }
      try {
        channel.dispatch(remote.getUnicastAddress(), CorusDiscoEvent.class.getName(), new CorusDiscoEvent(serverContext().getCorusHost()));
      } catch (IOException e) {
        log.debug("Event channel could not dispatch event", e);
      }

      // ------------------------------------------------------------------------
      // discovery event

    } else if (event instanceof CorusDiscoEvent) {
      CorusDiscoEvent evt = (CorusDiscoEvent) event;
      addNode(evt.getOrigin());
      if (log.isDebugEnabled()) {
        log.debug(String.format("Current hosts: %s", hostsByNode.values()));
      }
    }
  }

  @Override
  public synchronized void onDown(final EventChannelEvent event) {
    removeNode(event.getNode());
  }
  
  @Override
  public void onLeft(EventChannelEvent event) {
    removeNode(event.getNode());
  }

  @Override
  public void onUp(EventChannelEvent event) {
    try {
      // we want to make sure we're not sending a pub event prior to the
      // discovery process triggered at
      // startup having completed.
      if (System.currentTimeMillis() - startTime >= START_UP_DELAY) {
        log.debug("Corus appeared in cluster but not yet in host list; signaling presence to trigger discovery process");
        channel.dispatch(event.getAddress(), CorusPubEvent.class.getName(), new CorusPubEvent(serverContext().getCorusHost()));
      }
    } catch (IOException e) {
      log.debug("Error sending publish event", e);
    }
  }
  
  @Override
  public void onHeartbeatRequest(EventChannelEvent event) {
    onUp(event);
  }
  
  @Override
  public void onHeartbeatResponse(EventChannelEvent event) {
    onUp(event);
  }
  
  private void removeNode(String node) {
    CorusHost host = hostsByNode.remove(node);
    if (host != null) {
      log.info(String.format("Corus server left cluster: %s. Removing from cluster view", host));
    }
  }
  
  private void addNode(CorusHost host) {
    log.info(String.format("Corus server discovered: %s. Removing from cluster view", host.getEndpoint()));
    synchronized (hostsByNode) {   
      hostsByNode.put(host.getNode(), host);
    }
  }

}
