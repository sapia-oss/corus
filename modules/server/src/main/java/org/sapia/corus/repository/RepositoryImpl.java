package org.sapia.corus.repository;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.FilePath;
import org.sapia.corus.client.common.reference.AutoResetReference;
import org.sapia.corus.client.common.reference.Reference;
import org.sapia.corus.client.services.ModuleState;
import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.configurator.Property;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.port.PortManager;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.repository.ArtifactDeploymentRequest;
import org.sapia.corus.client.services.repository.ArtifactListRequest;
import org.sapia.corus.client.services.repository.ChangeRepoRoleNotification;
import org.sapia.corus.client.services.repository.ConfigNotification;
import org.sapia.corus.client.services.repository.DistributionDeploymentRequest;
import org.sapia.corus.client.services.repository.DistributionListResponse;
import org.sapia.corus.client.services.repository.ExecConfigNotification;
import org.sapia.corus.client.services.repository.FileDeploymentRequest;
import org.sapia.corus.client.services.repository.FileListResponse;
import org.sapia.corus.client.services.repository.ForceClientPullNotification;
import org.sapia.corus.client.services.repository.PortRangeNotification;
import org.sapia.corus.client.services.repository.Repository;
import org.sapia.corus.client.services.repository.RepositoryConfiguration;
import org.sapia.corus.client.services.repository.SecurityConfigNotification;
import org.sapia.corus.client.services.repository.ShellScriptDeploymentRequest;
import org.sapia.corus.client.services.repository.ShellScriptListResponse;
import org.sapia.corus.client.services.security.ApplicationKeyManager;
import org.sapia.corus.client.services.security.ApplicationKeyManager.AppKeyConfig;
import org.sapia.corus.client.services.security.SecurityModule;
import org.sapia.corus.client.services.security.SecurityModule.RoleConfig;
import org.sapia.corus.core.CorusConsts;
import org.sapia.corus.core.CorusReadonlyProperties;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.corus.core.ServerStartedEvent;
import org.sapia.corus.repository.task.ArtifactDeploymentRequestHandlerTask;
import org.sapia.corus.repository.task.ArtifactListRequestHandlerTask;
import org.sapia.corus.repository.task.DistributionListResponseHandlerTask;
import org.sapia.corus.repository.task.FileListResponseHandlerTask;
import org.sapia.corus.repository.task.ForcePullTask;
import org.sapia.corus.repository.task.GetArtifactListTask;
import org.sapia.corus.repository.task.HandleExecConfigTask;
import org.sapia.corus.repository.task.ShellScriptListResponseHandlerTask;
import org.sapia.corus.taskmanager.core.BackgroundTaskConfig;
import org.sapia.corus.taskmanager.core.SemaphoreThrottle;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskManager;
import org.sapia.corus.taskmanager.tasks.FileDeletionTask;
import org.sapia.corus.util.Queue;
import org.sapia.corus.util.TimeUtil;
import org.sapia.corus.util.DelayedQueue;
import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.mcast.SyncEventListener;
import org.sapia.ubik.net.ConnectionStateListener;
import org.sapia.ubik.rmi.Remote;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implements the {@link Repository} interface.
 * 
 * @author yduchesne
 * 
 */
@Bind(moduleInterface = { Repository.class })
@Remote(interfaces = { Repository.class })
public class RepositoryImpl extends ModuleHelper 
  implements 
    Repository, 
    AsyncEventListener, 
    SyncEventListener, 
    Interceptor, 
    ConnectionStateListener,
    java.rmi.Remote {


  private static final long DEFAULT_HANDLE_EXEC_CONFIG_DELAY        = TimeUnit.SECONDS.toMillis(1);
  private static final long DEFAULT_HANDLE_EXEC_CONFIG_INTERVAL     = TimeUnit.SECONDS.toMillis(3);
  private static final int  DEFAULT_HANDLE_EXEC_CONFIG_MAX_ATTEMPTS = 5;
  private static final long DEFAULT_IDLE_DELAY_SECONDS              = 60;
  private static final long DEFAULT_CHECK_INTERVAL_SECONDS          = 1;
  
  @Autowired
  private TaskManager taskManager;

  @Autowired
  private ClusterManager clusterManager;

  @Autowired
  private Configurator configurator;

  @Autowired
  private EventDispatcher dispatcher;

  @Autowired
  private PortManager portManager;
  
  @Autowired
  private SecurityModule securityModule;
  
  @Autowired
  private ApplicationKeyManager applicationKeys;

  private Queue<ArtifactListRequest> listRequests = new Queue<ArtifactListRequest>();
  private DelayedQueue<ArtifactDeploymentRequest> deployRequests;

  @Autowired
  private DeployerConfiguration   depoyerConfig;
  
  @Autowired
  private RepositoryConfiguration repoConfig;
  
  private Reference<ModuleState> state = new AutoResetReference<ModuleState>(
      ModuleState.IDLE, ModuleState.IDLE, TimeValue.createSeconds(DEFAULT_IDLE_DELAY_SECONDS)
  );

  public void setRepoConfig(RepositoryConfiguration repoConfig) {
    this.repoConfig = repoConfig;
  }
  
  public void setDepoyerConfig(DeployerConfiguration depoyerConfig) {
    this.depoyerConfig = depoyerConfig;
  }
  
  // --------------------------------------------------------------------------
  // Visible for testing

  void setClusterManager(ClusterManager clusterManager) {
    this.clusterManager = clusterManager;
  }

  void setConfigurator(Configurator configurator) {
    this.configurator = configurator;
  }

  void setDispatcher(EventDispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  void setTaskManager(TaskManager taskManager) {
    this.taskManager = taskManager;
  }

  void setPortManager(PortManager portManager) {
    this.portManager = portManager;
  }

  void setSecurityModule(SecurityModule securityModule) {
    this.securityModule = securityModule;
  }

  void setApplicationKeys(ApplicationKeyManager applicationKeys) {
    this.applicationKeys = applicationKeys;
  }

  void setDeployRequestQueue(DelayedQueue<ArtifactDeploymentRequest> deployRequests) {
    this.deployRequests = deployRequests;
  }

  void setArtifactListRequestQueue(Queue<ArtifactListRequest> listRequests) {
    this.listRequests = listRequests;
  }

  // --------------------------------------------------------------------------
  // Lifecycle

  @Override
  public void init() throws Exception {
    serverContext().getServices().bind(RepositoryConfiguration.class, repoConfig);
    deployRequests = new DelayedQueue<>(
        TimeValue.createSeconds(repoConfig.getArtifactDeploymentRequestActivityDelaySeconds()), 
        TimeValue.createSeconds(DEFAULT_CHECK_INTERVAL_SECONDS)
     );
    dispatcher.addInterceptor(ServerStartedEvent.class, this);
    clusterManager.getEventChannel().addConnectionStateListener(this);
    if (serverContext().getCorusHost().getRepoRole().isServer()) {
      logger().info("Node is repo server");
      taskManager.registerThrottle(ArtifactDeploymentRequestHandlerTask.DEPLOY_REQUEST_THROTTLE,
          new SemaphoreThrottle(repoConfig.getMaxConcurrentDeploymentRequests()));
      
      if (!repoConfig.isPushPropertiesEnabled()) {
        logger().info("Properties push is disabled");
      }
      if (!repoConfig.isPushTagsEnabled()) {
        logger().info("Tag push is disabled");
      }
      if (!repoConfig.isPushPortRangesEnabled()) {
        logger().info("Port range push is disabled");
      }
    } else if (serverContext().getCorusHost().getRepoRole().isClient()) {
      logger().info("Node is repo client");

      if (!repoConfig.isPullPropertiesEnabled()) {
        logger().info("Properties pull is disabled");
      }
      if (!repoConfig.isPullTagsEnabled()) {
        logger().info("Tag pull is disabled");
      }
      if (!repoConfig.isPullPortRangesEnabled()) {
        logger().info("Port range pull is disabled");
      }
      if (!repoConfig.isPullSecurityConfigEnabled()) {
        logger().info("Security config pull is disabled");
      }
      if (!repoConfig.isBootExecEnabled()) {
        logger().info("This node is configured NOT to perform automatic startup of processes with 'startOnBoot' enabled upon pull");
      }
    } else {
      logger().info("This node will not act as either a repository server or client");
    }
    doRegisterEventListeners();
    
    this.taskManager.executeBackground(new Task<Void, Void>("RepoFileCleanerTask") {
        @Override
        public Void execute(TaskExecutionContext ctx, Void param) throws Throwable {
          File repoDir = FilePath.forDirectory(depoyerConfig.getRepoDir()).createFile();
          long ttlMillis = TimeUnit.MINUTES.toMillis(repoConfig.getRepoFileTtlMinutes());
          for (File toCheck : repoDir.listFiles()) {
            if (System.currentTimeMillis() - toCheck.lastModified() >= ttlMillis) {
              ctx.debug("Deleting stale repo file: " + toCheck.getName());
              toCheck.delete();
            }
          }
          return null;
        }
      }, 
      null, 
      BackgroundTaskConfig.create().setExecInterval(
          TimeValue.createSeconds(repoConfig.getRepoFileCheckIntervalSeconds()).getValueInMillis()
      )
    );
  }
  
  @Override
  public void start() throws Exception {
    FileDeletionTask cleanRepoDir = new FileDeletionTask(
        "CleanRepoDirTask", 
        serverContext().getServices().getFileSystem().getFileHandle(depoyerConfig.getRepoDir()), 
        TimeUnit.MINUTES.toMillis(repoConfig.getRepoFileTtlMinutes())
    );
    
    taskManager.executeBackground(cleanRepoDir, null, 
      BackgroundTaskConfig.create().setExecInterval(
          TimeValue.createSeconds(repoConfig.getRepoFileCheckIntervalSeconds()).getValueInMillis()
      )
    );
  }
  
  private void doRegisterEventListeners() throws Exception {
    
    // all node types
    clusterManager.getEventChannel().registerAsyncListener(ChangeRepoRoleNotification.EVENT_TYPE, this);
    
    // repo server-related
    clusterManager.getEventChannel().registerAsyncListener(ArtifactListRequest.EVENT_TYPE, this);
    clusterManager.getEventChannel().registerAsyncListener(DistributionDeploymentRequest.EVENT_TYPE, this);
    clusterManager.getEventChannel().registerAsyncListener(FileDeploymentRequest.EVENT_TYPE, this);
    clusterManager.getEventChannel().registerAsyncListener(ShellScriptDeploymentRequest.EVENT_TYPE, this);

    // repo client-related
    clusterManager.getEventChannel().registerAsyncListener(DistributionListResponse.EVENT_TYPE, this);
    clusterManager.getEventChannel().registerAsyncListener(FileListResponse.EVENT_TYPE, this);
    clusterManager.getEventChannel().registerAsyncListener(ShellScriptListResponse.EVENT_TYPE, this);
    clusterManager.getEventChannel().registerSyncListener(ExecConfigNotification.EVENT_TYPE, this);
    clusterManager.getEventChannel().registerSyncListener(PortRangeNotification.EVENT_TYPE, this);
    clusterManager.getEventChannel().registerSyncListener(SecurityConfigNotification.EVENT_TYPE, this);
    clusterManager.getEventChannel().registerSyncListener(ConfigNotification.EVENT_TYPE, this);
  }

  @Override
  public void dispose() throws Exception {
  }

  // --------------------------------------------------------------------------
  // Module interface

  @Override
  public String getRoleName() {
    return ROLE;
  }
  
  // --------------------------------------------------------------------------
  // ConnectionStateListener interface
  
  @Override
  public void onConnected() {
  }
  
  @Override
  public void onDisconnected() {
  }
  
  @Override
  public void onReconnected() {
    doFirstPull();
  }

  // --------------------------------------------------------------------------
  // Repository interface

  
  @Override
  public Reference<ModuleState> getState() {
    return state;
  }
  
  @Override
  public void pull() throws IllegalStateException {
    state.set(ModuleState.BUSY);
    if (serverContext().getCorusHost().getRepoRole().isClient()) {
      logger().debug("Node is a repo client: will try to acquire distributions from repo server");
      GetArtifactListTask task = new GetArtifactListTask();
      task.setMaxExecution(repoConfig.getDistributionDiscoveryMaxAttempts());

      taskManager.executeBackground(task, null,
        BackgroundTaskConfig.create()
          .setExecInterval(TimeUnit.MILLISECONDS.convert(repoConfig.getDistributionDiscoveryIntervalSeconds(), TimeUnit.SECONDS)));
    }
  }

  @Override
  public void push() {
    state.set(ModuleState.BUSY);
    if (serverContext().getCorusHost().getRepoRole().isServer()) {
      ForceClientPullNotification notif = new ForceClientPullNotification(serverContext().getCorusHost().getEndpoint());
      for (CorusHost host : clusterManager.getHosts()) {
        if (host.getRepoRole().isClient()) {
          notif.addTarget(host.getEndpoint());
        }
      }
      try {
        clusterManager.send(notif);
      } catch (Exception e) {
        logger().error("Could not send pull notification", e);
      }
    }
  }
  
  @Override
  public synchronized void changeRole(RepoRole newRole) {
    log.debug("Setting new repo role: " + newRole);
    
    Properties props = new Properties();
    props.setProperty(CorusConsts.PROPERTY_REPO_TYPE, newRole.name().toLowerCase());

    Endpoint thisEndpoint = serverContext().getCorusHost().getEndpoint();
    CorusReadonlyProperties.save(
        props, 
        CorusConsts.CORUS_USER_HOME, 
        thisEndpoint.getServerTcpAddress().getPort(), 
        false
    );
    
    serverContext().getCorusHost().setRepoRole(newRole);
        
    ChangeRepoRoleNotification notif = new ChangeRepoRoleNotification(thisEndpoint, newRole);
    for (CorusHost host : clusterManager.getHosts()) {
      if (host.getRepoRole().isClient()) {
        notif.addTarget(host.getEndpoint());
      }
    }
    try {
      clusterManager.send(notif);
    } catch (Exception e) {
      logger().error("Could not send change-role notification", e);
    }
  }

  // -------------------------------------------------------------------------
  // Interceptor interface

  public void onServerStartedEvent(ServerStartedEvent event) {
    doFirstPull();
  }
  
  private void doFirstPull() {
    if (serverContext().getCorusHost().getRepoRole().isClient()) {
      state.set(ModuleState.BUSY);
      logger().debug("Node is a repo client: will request distributions from repository");
      Task<Void, Void> task = new ForcePullTask(this);
      task.executeOnce();
      long delay = TimeUtil.createRandomDelay(repoConfig.getBootstrapDelay());
      taskManager.executeBackground(task, null, BackgroundTaskConfig.create().setExecInterval(delay).setExecDelay(delay));
    } else {
      logger().debug(String.format("Node is %s, Will not pull distributions from repos", serverContext().getCorusHost().getRepoRole()));
    }    
  }
  
  // --------------------------------------------------------------------------
  // AsyncEventListener interface

  @Override
  public synchronized void onAsyncEvent(RemoteEvent evt) {
    RepoRole role = serverContext().getCorusHost().getRepoRole();
    try {
      if (evt.getType().equals(ArtifactListRequest.EVENT_TYPE) && role.isServer()) {
        logger().debug("Got artifact list request");
        state.set(ModuleState.BUSY);
        handleArtifactListRequest((ArtifactListRequest) evt.getData());

      // Distribution (list response, deployment request)
      } else if (evt.getType().equals(DistributionListResponse.EVENT_TYPE) && role.isClient()) {
        logger().debug("Got distribution list response");
        state.set(ModuleState.BUSY);
        handleDistributionListResponse((DistributionListResponse) evt.getData());
      } else if (evt.getType().equals(DistributionDeploymentRequest.EVENT_TYPE) && role.isServer()) {
        logger().debug("Got distribution deployment request");
        state.set(ModuleState.BUSY);
        handleDistributionDeploymentRequest((DistributionDeploymentRequest) evt.getData());

      // Shell script (list response, deployment request)
      } else if (evt.getType().equals(ShellScriptListResponse.EVENT_TYPE) && role.isClient()) {
        logger().debug("Got shell script list response");
        state.set(ModuleState.BUSY);
        handleShellScriptListResponse((ShellScriptListResponse) evt.getData());
      } else if (evt.getType().equals(ShellScriptDeploymentRequest.EVENT_TYPE) && role.isServer()) {
        logger().debug("Got shell script deployment request");
        state.set(ModuleState.BUSY);
        handleShellScriptDeploymentRequest((ShellScriptDeploymentRequest) evt.getData());

      // File (list response, deployment request)
      } else if (evt.getType().equals(FileListResponse.EVENT_TYPE) && role.isClient()) {
        logger().debug("Got file list response");
        state.set(ModuleState.BUSY);
        handleFileListResponse((FileListResponse) evt.getData());
      } else if (evt.getType().equals(FileDeploymentRequest.EVENT_TYPE) && role.isServer()) {
        logger().debug("Got file deployment request");
        state.set(ModuleState.BUSY);
        handleFileDeploymentRequest((FileDeploymentRequest) evt.getData());

      } else if (evt.getType().equals(ChangeRepoRoleNotification.EVENT_TYPE)) {
        logger().debug("Got repo role change notification");
        state.set(ModuleState.BUSY);
        handleChangeRoleNotification((ChangeRepoRoleNotification) evt.getData());
        
      } else {
        logger().debug("Unknown event type: " + evt.getType());
      }
    } catch (IOException e) {
      logger().error("IO Error caught trying to handle event: " + evt.getType(), e);
    }
  }

  @Override
  public Object onSyncEvent(RemoteEvent evt) {
    RepoRole role = serverContext().getCorusHost().getRepoRole();
    try {
      if (evt.getType().equals(ExecConfigNotification.EVENT_TYPE) && role.isClient()) {
        logger().debug("Got exec config notification");
        state.set(ModuleState.BUSY);
        handleExecConfigNotification((ExecConfigNotification) evt.getData());
      } else if (evt.getType().equals(ConfigNotification.EVENT_TYPE) && role.isClient()) {
        logger().debug("Got config notification");
        state.set(ModuleState.BUSY);
        handleConfigNotification((ConfigNotification) evt.getData());
      } else if (evt.getType().equals(PortRangeNotification.EVENT_TYPE) && role.isClient()) {
        logger().debug("Got port range notification");
        state.set(ModuleState.BUSY);
        handlePortRangeNotification((PortRangeNotification) evt.getData());
      } else if (evt.getType().equals(SecurityConfigNotification.EVENT_TYPE) && role.isClient()) {
        logger().debug("Got security config notification");
        state.set(ModuleState.BUSY);
        handleSecurityConfigNotification((SecurityConfigNotification) evt.getData());
      }
    } catch (IOException e) {
      logger().error("IO Error caught trying to handle event: " + evt.getType(), e);
    }
    return null;
  }

  // --------------------------------------------------------------------------
  // Restricted methods (event handlers)

  void handleExecConfigNotification(ExecConfigNotification notif) {
    if (notif.isTargeted(serverContext().getCorusHost().getEndpoint())) {
      if (notif.getConfigs().isEmpty()) {
        logger().debug("Received empty exec config list");
        return;
      } else {
        for (ExecConfig config : notif.getConfigs()) {
          logger().debug("Got exec config: " + config.getName());
        }
      }

      taskManager.executeBackground(
          new HandleExecConfigTask(repoConfig, notif.getConfigs())
              .setMaxExecution(DEFAULT_HANDLE_EXEC_CONFIG_MAX_ATTEMPTS), 
          null,
          BackgroundTaskConfig.create()
            .setExecDelay(DEFAULT_HANDLE_EXEC_CONFIG_DELAY)
            .setExecInterval(DEFAULT_HANDLE_EXEC_CONFIG_INTERVAL)
      );
    }

    // cascading to next host
    try {
      clusterManager.send(notif);
    } catch (Exception e) {
      logger().error("Could not cascade notification to next host", e);
    }
  }

  void handleConfigNotification(ConfigNotification notif) {
    if (notif.isTargeted(serverContext().getCorusHost().getEndpoint())) {
      logger().info(String.format("Adding config %s", notif));

      if (logger().isDebugEnabled()) {
        List<Property> props = notif.getProperties();
        for (Property p : props) {
          logger().debug(String.format("Property %s = %s (category: %s)", 
              p.getName(), p.getValue(), p.getCategory().isNull() ? "N/A" : p.getCategory().get()));
        }
      }

      if (repoConfig.isPullPropertiesEnabled()) {
        for (Property p : notif.getProperties()) {
          if (p.getCategory().isNull()) {
            configurator.addProperty(PropertyScope.PROCESS, p.getName(), p.getValue(), new HashSet<String>());
          } else {
            configurator.addProperty(PropertyScope.PROCESS, p.getName(), p.getValue(), Collects.arrayToSet(p.getCategory().get()));
          }
        }
      } else {
        logger().info("Aborting adding properties: node does not support pull of properties");
      }

      if (logger().isDebugEnabled()) {
        for (String t : notif.getTags()) {
          logger().debug("tag: " + t);
        }
      }

      if (repoConfig.isPullTagsEnabled()) {
        configurator.addTags(notif.getTags(), false);
      } else {
        logger().info("Aborting adding tags: node does not support pull of tags");
      }
    }

    // cascading to next host
    try {
      clusterManager.send(notif);
    } catch (Exception e) {
      logger().error("Could not cascade notification to next host", e);
    }
  }

  void handlePortRangeNotification(PortRangeNotification notif) {
    if (notif.isTargeted(serverContext().getCorusHost().getEndpoint()) && repoConfig.isPullPortRangesEnabled()) {
      logger().info(String.format("Adding port ranges %s", notif));
      if (!notif.getPortRanges().isEmpty()) {
        try {
          Map<String, PortRange> currentRangesByName = new HashMap<String, PortRange>();
          for (PortRange c : portManager.getPortRanges()) {
            currentRangesByName.put(c.getName(), c);
          }

          for (PortRange r : notif.getPortRanges()) {
            PortRange current = currentRangesByName.get(r.getName());
            if (current != null) {
              if (current.getMin() == r.getMin() && current.getMax() == r.getMax()) {
                logger().info("This node already has range, so not adding: " + r);
              } else if (!current.getActive().isEmpty()) {
                logger().warn("This node has range with same and ports currently active, so not adding: " + r);
              } else {
                logger().warn("Adding new port range version: " + r);
                portManager.updatePortRange(r.getName(), r.getMin(), r.getMax());
              }
            } else {
              logger().warn("Adding new port range: " + r);
              portManager.updatePortRange(r.getName(), r.getMin(), r.getMax());
            }
          }
        } catch (Exception e) {
          logger().error("Could not add port ranges", e);
        }
      }
    }

    // cascading to next host
    try {
      clusterManager.send(notif);
    } catch (Exception e) {
      logger().error("Could not cascade notification to next host", e);
    }

  }
  
  void handleSecurityConfigNotification(SecurityConfigNotification notif) {
    if (notif.isTargeted(serverContext().getCorusHost().getEndpoint()) 
        && repoConfig.isPullSecurityConfigEnabled()) {
      if (!notif.getRoleConfigurations().isEmpty()) {
        logger().info("Adding roles");
        for (RoleConfig rc : notif.getRoleConfigurations()) {
          securityModule.addOrUpdateRole(rc.getRole(), rc.getPermissions());
        }
      }
      
      if (!notif.getAppKeyConfigurations().isEmpty()) {
        logger().info("Adding application keys");
        for (AppKeyConfig apk : notif.getAppKeyConfigurations()) {
          applicationKeys.addOrUpdateApplicationKey(apk.getAppId(), apk.getApplicationKey(), apk.getRole());
        }
      }
    }
    
    // cascading to next host
    try {
      clusterManager.send(notif);
    } catch (Exception e) {
      logger().error("Could not cascade notification to next host", e);
    }
  }
  
  void handleChangeRoleNotification(ChangeRepoRoleNotification notif) {
    if (notif.isTargeted(serverContext().getCorusHost().getEndpoint())) {
      for (CorusHost h : clusterManager.getHosts()) {
        if (h.getEndpoint().equals(notif.getRepoEndpoint())) {
          h.setRepoRole(notif.getNewRole());
        }
      }
    }
    
    // cascading to next host
    try {
      clusterManager.send(notif);
    } catch (Exception e) {
      logger().error("Could not cascade notification to next host", e);
    }
  }
  
  // --------------------------------------------------------------------------
  // Artifact list request

  void handleArtifactListRequest(ArtifactListRequest distsReq) {
    if (serverContext().getCorusHost().getRepoRole().isServer()) {
      listRequests.add(distsReq);
      taskManager.execute(new ArtifactListRequestHandlerTask(repoConfig, listRequests), null);
    } else {
      logger().debug("Ignoring " + distsReq + "; repo type is " + serverContext().getCorusHost().getRepoRole());
    }
  }

  // --------------------------------------------------------------------------
  // Distribution

  void handleDistributionListResponse(final DistributionListResponse distsRes) {
    if (serverContext().getCorusHost().getRepoRole().isClient()) {
      taskManager.execute(new DistributionListResponseHandlerTask(distsRes), null);
    } else {
      logger().debug("Ignoring " + distsRes + "; repo type is " + serverContext().getCorusHost().getRepoRole());
    }
  }

  void handleDistributionDeploymentRequest(DistributionDeploymentRequest req) {
    if (serverContext().getCorusHost().getRepoRole().isServer()) {
      deployRequests.add(req);
      taskManager.execute(new ArtifactDeploymentRequestHandlerTask(repoConfig, deployRequests), null);
    } else {
      logger().debug("Ignoring " + req + "; repo type is " + serverContext().getCorusHost().getRepoRole());
    }
  }

  // --------------------------------------------------------------------------
  // ShellScript

  void handleShellScriptListResponse(final ShellScriptListResponse response) {
    if (!repoConfig.isPullScriptsEnabled()) {
      logger().debug("Ignoring " + response + "; script pull is disabled");
    } else if (serverContext().getCorusHost().getRepoRole().isClient()) {
      taskManager.execute(new ShellScriptListResponseHandlerTask(response), null);
    } else {
      logger().debug("Ignoring " + response + "; repo type is " + serverContext().getCorusHost().getRepoRole());
    }
  }

  void handleShellScriptDeploymentRequest(ShellScriptDeploymentRequest req) {
    if (serverContext().getCorusHost().getRepoRole().isServer()) {
      deployRequests.add(req);
      taskManager.execute(new ArtifactDeploymentRequestHandlerTask(repoConfig, deployRequests), null);
    } else {
      logger().debug("Ignoring " + req + "; repo type is " + serverContext().getCorusHost().getRepoRole());
    }
  }

  // --------------------------------------------------------------------------
  // File

  void handleFileListResponse(final FileListResponse response) {
    if (!repoConfig.isPullFilesEnabled()) {
      logger().debug("Ignoring " + response + "; file pull is disabled");
    } else if (serverContext().getCorusHost().getRepoRole().isClient()) {
      taskManager.execute(new FileListResponseHandlerTask(response), null);
    } else {
      logger().debug("Ignoring " + response + "; repo type is " + serverContext().getCorusHost().getRepoRole());
    }
  }

  void handleFileDeploymentRequest(FileDeploymentRequest req) {
    if (serverContext().getCorusHost().getRepoRole().isServer()) {
      deployRequests.add(req);
      taskManager.execute(new ArtifactDeploymentRequestHandlerTask(repoConfig, deployRequests), null);
    } else {
      logger().debug("Ignoring " + req + "; repo type is " + serverContext().getCorusHost().getRepoRole());
    }
  }

}