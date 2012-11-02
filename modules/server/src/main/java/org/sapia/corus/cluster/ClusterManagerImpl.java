package org.sapia.corus.cluster;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.services.Service;
import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.cluster.ClusterStatus;
import org.sapia.corus.client.services.cluster.ServerHost;
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
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Remote;
import org.sapia.ubik.rmi.server.Hub;
import org.sapia.ubik.rmi.server.transport.IncomingCommandEvent;
import org.sapia.ubik.util.Props;


/**
 * Implements the {@link ClusterManager} module.
 * 
 * @author Yanick Duchesne
 */
@Bind(moduleInterface=ClusterManager.class)
@Remote(interfaces=ClusterManager.class)
public class ClusterManagerImpl extends ModuleHelper
  implements ClusterManager, AsyncEventListener, EventChannelStateListener {
   
  private static final int  MAX_PUB_EXEC            = 2;
  private static final int  PUB_DELAY_RANGE         = 500;
  private static final int  PUB_DELAY_RANGE_BASE    = 100;
  private static final int  PUB_INTERVAL_RANGE      = 3000;
  private static final int  PUB_INTERVAL_RANGE_BASE = 8000;
  private static final long START_UP_DELAY          = 15000;
  
  private EventChannel                 channel;
  private Set<ServerAddress>           hostsAddresses   = Collections.synchronizedSet(new HashSet<ServerAddress>());
  private Set<ServerHost>              hostsInfos       = Collections.synchronizedSet(new HashSet<ServerHost>());
  private Map<String, ServerHost>      hostsByNode      = Collections.synchronizedMap(new HashMap<String, ServerHost>());
  private ServerSideClusterInterceptor interceptor;
  private long                         startTime 				= System.currentTimeMillis();
  
  /**
   * @see Service#init()
   */
  public void init() throws Exception {
    
    channel = new EventChannel(
    		serverContext().getDomain(), 
    		new Props().addProperties(serverContext().getCorusProperties()).addSystemProperties()
    );
    
    channel.registerAsyncListener(CorusPubEvent.class.getName(), this);
    channel.registerAsyncListener(CorusDiscoEvent.class.getName(), this);
    channel.addEventChannelStateListener(this);
    channel.start();
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void start() throws Exception {
    super.start();
    interceptor = new ServerSideClusterInterceptor(log, serverContext());
    Hub.getModules().getServerRuntime().addInterceptor(IncomingCommandEvent.class, interceptor);

    if(log.isInfoEnabled()) {
      log.info("Signaling presence to cluster");
      Properties mcastProperties = PropertiesUtil.filter(
          System.getProperties(), 
          PropertiesFilter.NameContainsPropertiesFilter.createInstance("mcast")
      );
      Enumeration<String> names = (Enumeration<String>) mcastProperties.propertyNames();
      while(names.hasMoreElements()) {
        String name = names.nextElement();
        log.info(name + "=" + mcastProperties.getProperty(names.nextElement()));
      }
    }
    
    super.serverContext().getServices().getTaskManager().executeBackground(
        new PublishToClusterTask(), 
        null, 
        BackgroundTaskConfig.create()
          .setExecDelay(PUB_DELAY_RANGE_BASE + new Random().nextInt(PUB_DELAY_RANGE))
          .setExecInterval(PUB_INTERVAL_RANGE_BASE + new Random().nextInt(PUB_INTERVAL_RANGE))
    );
    
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
    return new HashSet<ServerAddress>(hostsAddresses);
  }

  @Override
  public synchronized Set<ServerHost> getHosts() {
    return new HashSet<ServerHost>(hostsInfos);
  }
  
  @Override
  public ClusterStatus getClusterStatus() {
    return new ClusterStatus(channel.getRole(), this.serverContext.getCorus().getHostInfo());
  }

  @Override
  public EventChannel getEventChannel() {
    return channel;
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
      CorusPubEvent evt  = (CorusPubEvent) event;
      ServerAddress  addr = evt.getOrigin();
      
      if(hostsAddresses.add(evt.getOrigin())){
        log.info(String.format("Corus discovered at %s", addr));        
        hostsInfos.add(evt.getHostInfo());
      } else{
        log.info(String.format("Corus discovered at %s; already registered (that node probably was restarted): ", addr));
      }
      
      log.debug(String.format("Current addresses: %s", hostsAddresses));
      
      hostsByNode.put(remote.getNode(), evt.getHostInfo());
      
      try {
        channel.dispatch(
            remote.getUnicastAddress(),
            CorusDiscoEvent.class.getName(),
            new CorusDiscoEvent(
                serverContext().getTransport().getServerAddress(), 
                serverContext().getHostInfo()
            )
        );
      } catch (IOException e) {
        log.debug("Event channel could not dispatch event", e);
      }
      
    // ------------------------------------------------------------------------
    // discovery event
      
    } else if (event instanceof CorusDiscoEvent) {
      CorusDiscoEvent evt  = (CorusDiscoEvent) event;
      ServerAddress  addr = evt.getOrigin();
      if(hostsAddresses.add(evt.getOrigin())) {
      	log.debug(String.format("Existing corus discovered: %s", addr));
      	hostsInfos.add(evt.getHostInfo());
      } 
      log.debug(String.format("Current addresses: %s", hostsAddresses));
      hostsByNode.put(remote.getNode(), evt.getHostInfo());      
    }
  }
  
  @Override
  public synchronized void onDown(final EventChannelEvent event) {
    synchronized(hostsByNode){
      ServerHost host = hostsByNode.remove(event.getNode());
      if(host != null){
        log.info(String.format("Corus server detected as down: %s. Removing from cluster view", 
            host.getServerAddress()));        
        synchronized(hostsAddresses){
          hostsAddresses.remove(host.getServerAddress());
        }
        synchronized(hostsInfos){
          hostsInfos.remove(host);
        }
      }
    }

    Thread async = new Thread(getClass().getSimpleName()+"Discovery@"+event.getAddress()){
      
      @Override
      public void run() {
        try {
          log.debug(String.format("Trying to trigger discovery of down Corus server %s", event.getAddress()));
          channel.dispatch(
              event.getAddress(), CorusPubEvent.class.getName(),
              new CorusPubEvent(
                  serverContext().getServerAddress(), 
                  serverContext().getHostInfo()
              )
          );
        }catch(IOException e) {
          log.error(String.format("Could not send pub event to %s", event.getAddress()), e);
        }
      }
    };
    async.setDaemon(true);
    async.run();
  }
  
  @Override
  public void onUp(EventChannelEvent event) {
    if(!hostsByNode.containsKey(event.getNode())){
      try{
        // we want to make sure we're not sending a pub event prior to the discovery process triggered at
        // startup having completed.
        if(System.currentTimeMillis() - startTime >=  START_UP_DELAY){
          log.debug("Corus appeared in cluster but not yet in host list; signaling presence to trigger discovery process");
          channel.dispatch(
              event.getAddress(), CorusPubEvent.class.getName(),
              new CorusPubEvent(
                  serverContext().getServerAddress(), 
                  serverContext().getHostInfo()
              )
          );
        }
      }catch(IOException e){
        log.error("Error sending publish event", e);
      }
    }
  }
  
  // --------------------------------------------------------------------------
  
  /**
   * Signals presence to cluster. 
   */
  public class PublishToClusterTask extends Task<Void, Void> {
    
    public PublishToClusterTask() {
      super.setMaxExecution(MAX_PUB_EXEC);
    }
    
    @Override
    protected void onMaxExecutionReached(TaskExecutionContext ctx)
        throws Throwable {
      log.debug("Completed signaling presence to cluster");
    }
    
    @Override
    public Void execute(TaskExecutionContext ctx, Void param) throws Throwable {
      
      log.debug("Dispatching cluster presence event (attempt: " + getExecutionCount() + ")");
      
      channel.dispatch(
          CorusPubEvent.class.getName(),
          new CorusPubEvent(
              serverContext().getServerAddress(), 
              serverContext().getHostInfo()
          )
      );      
      return null;
    }
    
  }

}
