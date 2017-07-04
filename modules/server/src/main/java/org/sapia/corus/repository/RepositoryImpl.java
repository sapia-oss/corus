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
import org.sapia.corus.client.services.cluster.event.CorusHostAddedEvent;
import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.configurator.Property;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.DeployerConfiguration;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.event.CascadingDeploymentInterruptedEvent;
import org.sapia.corus.client.services.diagnostic.SystemDiagnosticCapable;
import org.sapia.corus.client.services.diagnostic.SystemDiagnosticResult;
import org.sapia.corus.client.services.diagnostic.SystemDiagnosticStatus;
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
import org.sapia.corus.client.services.repository.PullNotification;
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
import org.sapia.corus.repository.task.CheckNodeStateTask;
import org.sapia.corus.repository.task.DistributionListResponseHandlerTask;
import org.sapia.corus.repository.task.FileListResponseHandlerTask;
import org.sapia.corus.repository.task.ForceGetArtifactListTask;
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
import org.sapia.corus.util.DelayedQueue;
import org.sapia.corus.util.Queue;
import org.sapia.corus.util.TimeUtil;
import org.sapia.ubik.mcast.AsyncEventListener;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.net.ConnectionStateListener;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.Remote;
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
    SystemDiagnosticCapable,
    AsyncEventListener,
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
  private Deployer deployer;
  
  @Autowired
  private PortManager portManager;
  
  @Autowired
  private SecurityModule securityModule;
  
  @Autowired
  private ApplicationKeyManager applicationKeys;

  private Queue<ArtifactListRequest> listRequests = new Queue<ArtifactListRequest>();
  private DelayedQueue<ArtifactDeploymentRequest> deployRequests;

  @Autowired
  private DeployerConfiguration depoyerConfig;
  
  @Autowired
  private RepositoryConfiguration repoConfig;
  
  private volatile RepoStrategy strategy;
  
  private Reference<ModuleState> state = new AutoResetReference<ModuleState>(
      ModuleState.IDLE, ModuleState.IDLE, TimeValue.createSeconds(DEFAULT_IDLE_DELAY_SECONDS)
  );
  
  private PullProcessState pullProcessState = new PullProcessState();

  private Object lock = new Object();
  
  private volatile boolean checkNodeStateTaskStarted;
  
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
  
  void setDeployer(Deployer deployer) {
    this.deployer = deployer;
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
  
  void setRepoStrategy(RepoStrategy strategy) {
    this.strategy = strategy;
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
    dispatcher.addInterceptor(CascadingDeploymentInterruptedEvent.class, this);
    clusterManager.getEventChannel().addConnectionStateListener(this);
    initStrategy();
    
    taskManager.registerThrottle(ArtifactDeploymentRequestHandlerTask.DEPLOY_REQUEST_THROTTLE,
        new SemaphoreThrottle(repoConfig.getMaxConcurrentDeploymentRequests()));
    
    if (serverContext().getCorusHost().getRepoRole().isServer()) {
      logger().info("Node is repo server");
      
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
      
      // only start explicitely in this case: we want
      startCheckNodeTask();
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
    clusterManager.getEventChannel().registerAsyncListener(PullNotification.EVENT_TYPE, this);
    
    // repo server-related
    clusterManager.getEventChannel().registerAsyncListener(ArtifactListRequest.EVENT_TYPE, this);
    clusterManager.getEventChannel().registerAsyncListener(DistributionDeploymentRequest.EVENT_TYPE, this);
    clusterManager.getEventChannel().registerAsyncListener(FileDeploymentRequest.EVENT_TYPE, this);
    clusterManager.getEventChannel().registerAsyncListener(ShellScriptDeploymentRequest.EVENT_TYPE, this);

    // repo client-related
    clusterManager.getEventChannel().registerAsyncListener(DistributionListResponse.EVENT_TYPE, this);
    clusterManager.getEventChannel().registerAsyncListener(FileListResponse.EVENT_TYPE, this);
    clusterManager.getEventChannel().registerAsyncListener(ShellScriptListResponse.EVENT_TYPE, this);
    clusterManager.getEventChannel().registerAsyncListener(ExecConfigNotification.EVENT_TYPE, this);
    clusterManager.getEventChannel().registerAsyncListener(PortRangeNotification.EVENT_TYPE, this);
    clusterManager.getEventChannel().registerAsyncListener(SecurityConfigNotification.EVENT_TYPE, this);
    clusterManager.getEventChannel().registerAsyncListener(ConfigNotification.EVENT_TYPE, this);
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
  // Event interceptor methods
  
  public void onServerStartedEvent(ServerStartedEvent event) {
    dispatcher.addInterceptor(CorusHostAddedEvent.class, this);
    doFirstPull();
  }
  
  public void onCorusHostAddedEvent(CorusHostAddedEvent event) {
    if (event.getHost().getRepoRole() == RepoRole.SERVER
        && strategy.acceptsPull() 
        && !state.get().isBusy() 
        && deployer.getDistributions(DistributionCriteria.builder().all()).isEmpty()) {
      doFirstPull();
    }
  }
  
  public void onCascadingDeploymentInterruptedEvent(CascadingDeploymentInterruptedEvent event) {
    try {
      PullNotification  pull  = new PullNotification().setForce(true).setSource(serverContext().getCorusHost().getEndpoint());
      Map<ServerAddress, Endpoint> hosts = new HashMap<>();
      for (CorusHost host : clusterManager.getHosts()) {
        hosts.put(host.getEndpoint().getServerAddress(), host.getEndpoint());
      }
      
      for (ServerAddress addr : event.getRemainingHosts()) {
        Endpoint target = hosts.get(addr);
        if (target != null) {
          pull.addTarget(target);
        }
      }
      
      clusterManager.dispatch(pull);
    } catch (IOException e) {
      logger().warn("Could not send pull notification", e);
    }
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
    if (strategy.acceptsPull()) {
      logger().debug("Node is a repo client or a repo server that accepts being synchronized: will try to acquire distributions from repo server");
      GetArtifactListTask task = new GetArtifactListTask(pullProcessState, () -> startCheckNodeTask());
      task.setMaxExecution(repoConfig.getDistributionDiscoveryMaxAttempts());

      BackgroundTaskConfig taskConf = BackgroundTaskConfig.create()
          .setExecInterval(TimeUnit.MILLISECONDS.convert(repoConfig.getDistributionDiscoveryIntervalSeconds(), TimeUnit.SECONDS));
      
      taskManager.executeBackground(task, null, taskConf);
    }
  }

  @Override
  public void push() {
    state.set(ModuleState.BUSY);
    if (serverContext().getCorusHost().getRepoRole().isServer()) {
      PullNotification notif = new PullNotification();
      for (CorusHost host : clusterManager.getHosts()) {
        if (host.getRepoRole().isClient()) {
          notif.addTarget(host.getEndpoint());
        }
      }
      try {
        clusterManager.dispatch(notif);
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
    initStrategy();
        
    ChangeRepoRoleNotification notif = new ChangeRepoRoleNotification(thisEndpoint, newRole);
    for (CorusHost host : clusterManager.getHosts()) {
      if (host.getRepoRole().isClient()) {
        notif.addTarget(host.getEndpoint());
      }
    }
    try {
      clusterManager.dispatch(notif);
    } catch (Exception e) {
      logger().error("Could not send change-role notification", e);
    }
  }
  
  private synchronized void doFirstPull() {
    if (strategy.acceptsPull() && !state.get().isBusy()) {
      state.set(ModuleState.BUSY);
      logger().debug("Node is a repo client or a repo server that accepts being synchronized: will request distributions from repository");
      Task<Void, Void> task = new ForcePullTask(this);
      task.executeOnce();
      long delay = TimeUtil.createRandomDelay(repoConfig.getBootstrapDelay());
      BackgroundTaskConfig taskConf = BackgroundTaskConfig.create().setExecInterval(delay).setExecDelay(delay);
      taskManager.executeBackground(task, null, taskConf);
    } else {
      logger().debug(String.format("Node is %s, Will not pull distributions from repos", serverContext().getCorusHost().getRepoRole()));
    }    
  }
  
  // --------------------------------------------------------------------------
  // SystemDiagnosticCapable interface
  
  @Override
  public SystemDiagnosticResult getSystemDiagnostic() {
    if (getState().get() == ModuleState.BUSY) {
      return new SystemDiagnosticResult("Repository", SystemDiagnosticStatus.BUSY, "Currently performing replication");
    } else {
      return new SystemDiagnosticResult("Repository", SystemDiagnosticStatus.UP);
    }
  }

  // --------------------------------------------------------------------------
  // AsyncEventListener interface

  @Override
  public synchronized void onAsyncEvent(RemoteEvent evt) {
    try {
      if (evt.getType().equals(ArtifactListRequest.EVENT_TYPE)) {
        ArtifactListRequest request = (ArtifactListRequest) evt.getData();
        if (strategy.acceptsEvent(RepoEventType.ARTIFACT_LIST_REQUEST) || request.isForce()) {
          logger().debug("Got artifact list request");
          state.set(ModuleState.BUSY);
          handleArtifactListRequest(request);
        }

      // Distribution (list response, deployment request)
      } else if (evt.getType().equals(DistributionListResponse.EVENT_TYPE)) {
        DistributionListResponse response = (DistributionListResponse) evt.getData();
        if (strategy.acceptsEvent(RepoEventType.DISTRIBUTION_LIST_RESPONSE) || response.isForce()) {
          logger().debug("Got distribution list response");
          state.set(ModuleState.BUSY);
          handleDistributionListResponse(response);
        }
        
      } else if (evt.getType().equals(DistributionDeploymentRequest.EVENT_TYPE)) {
        DistributionDeploymentRequest request = (DistributionDeploymentRequest) evt.getData();
        if (strategy.acceptsEvent(RepoEventType.DISTRIBUTION_DEPLOYMENT_REQUEST) || request.isForce()) { 
          logger().debug("Got distribution deployment request");
          state.set(ModuleState.BUSY);
          handleDistributionDeploymentRequest(request);
        }
      // Shell script (list response, deployment request)
      } else if (evt.getType().equals(ShellScriptListResponse.EVENT_TYPE)) {
        ShellScriptListResponse response = (ShellScriptListResponse) evt.getData();
        if (strategy.acceptsEvent(RepoEventType.SHELL_SCRIPT_LIST_RESPONSE) || response.isForce()) {
          logger().debug("Got shell script list response");
          state.set(ModuleState.BUSY);
          handleShellScriptListResponse(response);
        }
        
      } else if (evt.getType().equals(ShellScriptDeploymentRequest.EVENT_TYPE)) {
        ShellScriptDeploymentRequest request = (ShellScriptDeploymentRequest) evt.getData();
        if (strategy.acceptsEvent(RepoEventType.SHELL_SCRIPT_DEPLOYMENT_REQUEST) || request.isForce()) {       
          logger().debug("Got shell script deployment request");
          state.set(ModuleState.BUSY);
          handleShellScriptDeploymentRequest(request);
        }

      // File (list response, deployment request)
      } else if (evt.getType().equals(FileListResponse.EVENT_TYPE)) {
        FileListResponse response = (FileListResponse) evt.getData();
        if (strategy.acceptsEvent(RepoEventType.FILE_LIST_RESPONSE) || response.isForce()) {
          logger().debug("Got file list response");
          state.set(ModuleState.BUSY);
          handleFileListResponse(response);
        }
      } else if (evt.getType().equals(FileDeploymentRequest.EVENT_TYPE)) {
        FileDeploymentRequest request = (FileDeploymentRequest) evt.getData();
        if (strategy.acceptsEvent(RepoEventType.FILE_DEPLOYMENT_REQUEST) || request.isForce()) {
          logger().debug("Got file deployment request");
          state.set(ModuleState.BUSY);
          handleFileDeploymentRequest(request);
        }

      } else if (evt.getType().equals(ChangeRepoRoleNotification.EVENT_TYPE)) {
        logger().debug("Got repo role change notification");
        state.set(ModuleState.BUSY);
        handleChangeRoleNotification((ChangeRepoRoleNotification) evt.getData());
        
      } else if (evt.getType().equals(PullNotification.EVENT_TYPE)) {
        logger().debug("Got forced pull notification");
        state.set(ModuleState.BUSY);
        handlePullNotification((PullNotification) evt.getData()); 
      
      } else if (evt.getType().equals(ExecConfigNotification.EVENT_TYPE)) {
        ExecConfigNotification notif = (ExecConfigNotification) evt.getData();
        if (strategy.acceptsEvent(RepoEventType.EXEC_CONFIG_NOTIFICATION) || notif.isForce()) {
          logger().debug("Got exec config notification");
          state.set(ModuleState.BUSY);
          handleExecConfigNotification(notif);
        }
        
      } else if (evt.getType().equals(ConfigNotification.EVENT_TYPE)) {
        ConfigNotification notif = (ConfigNotification) evt.getData();
        if (strategy.acceptsEvent(RepoEventType.CONFIG_NOTIFICATION) || notif.isForce()) {
          logger().debug("Got config notification");
          state.set(ModuleState.BUSY);
          handleConfigNotification(notif);
        }
        
      } else if (evt.getType().equals(PortRangeNotification.EVENT_TYPE)) {
        PortRangeNotification notif = (PortRangeNotification) evt.getData();
        if (strategy.acceptsEvent(RepoEventType.PORT_RANGE_NOTIFICATION) || notif.isForce()) {
          logger().debug("Got port range notification");
          state.set(ModuleState.BUSY);
          handlePortRangeNotification(notif);
        }
        
      } else if (evt.getType().equals(SecurityConfigNotification.EVENT_TYPE)) {
        SecurityConfigNotification notif = (SecurityConfigNotification) evt.getData();
        if (strategy.acceptsEvent(RepoEventType.SECURITY_CONFIG_NOTIFICATION) || notif.isForce()) {
          logger().debug("Got security config notification");
          state.set(ModuleState.BUSY);
          handleSecurityConfigNotification(notif);
        }
        
      } else {
        logger().debug("Unknown event type: " + evt.getType());
      }
    } catch (IOException e) {
      logger().error("IO Error caught trying to handle event: " + evt.getType(), e);
    }
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
      clusterManager.dispatch(notif);
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

      if (repoConfig.isPullPropertiesEnabled() || notif.isForce()) {
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
      clusterManager.dispatch(notif);
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
      clusterManager.dispatch(notif);
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
      clusterManager.dispatch(notif);
    } catch (Exception e) {
      logger().error("Could not cascade notification to next host", e);
    }
  }
  
  void handleChangeRoleNotification(ChangeRepoRoleNotification notif) {
    if (notif.isTargeted(serverContext().getCorusHost().getEndpoint())) {
      for (CorusHost h : clusterManager.getHosts()) {
        if (h.getEndpoint().equals(notif.getRepoEndpoint())) {
          h.setRepoRole(notif.getNewRole());
          initStrategy();
        }
      }
    }
    
    // cascading to next host
    try {
      clusterManager.dispatch(notif);
    } catch (Exception e) {
      logger().error("Could not cascade notification to next host", e);
    }
  }
  
  void handlePullNotification(PullNotification notif) {
    if (notif.isTargeted(serverContext().getCorusHost().getEndpoint()) && (strategy.acceptsPull() || notif.isForce())) {
      logger().debug("Forced pull notification received: pull will be triggered");
      
      if (notif.isForce()) {
        ForceGetArtifactListTask task = new ForceGetArtifactListTask(notif.getSource(), pullProcessState, () -> startCheckNodeTask());
        task.setMaxExecution(repoConfig.getDistributionDiscoveryMaxAttempts());
  
        BackgroundTaskConfig taskConf = BackgroundTaskConfig.create()
            .setExecInterval(TimeUnit.MILLISECONDS.convert(repoConfig.getDistributionDiscoveryIntervalSeconds(), TimeUnit.SECONDS));
        
        taskManager.executeBackground(task , null, taskConf);
      } else {
        pull();
      }
    }
  }
  
  // --------------------------------------------------------------------------
  // Artifact list request

  void handleArtifactListRequest(ArtifactListRequest distsReq) {
    if (serverContext().getCorusHost().getRepoRole().isServer() || distsReq.isForce()) {
      listRequests.add(distsReq);
      taskManager.execute(new ArtifactListRequestHandlerTask(repoConfig, listRequests), null);
    } else {
      logger().debug("Ignoring " + distsReq + "; repo type is " + serverContext().getCorusHost().getRepoRole());
    }
  }

  // --------------------------------------------------------------------------
  // Distribution

  void handleDistributionListResponse(final DistributionListResponse distsRes) {
    if (strategy.acceptsPull() || distsRes.isForce()) {
      taskManager.execute(new DistributionListResponseHandlerTask(distsRes, pullProcessState), null);
    } else {
      logger().debug("Ignoring " + distsRes + "; repo type is " + serverContext().getCorusHost().getRepoRole());
    }
  }

  void handleDistributionDeploymentRequest(DistributionDeploymentRequest req) {
    if (serverContext().getCorusHost().getRepoRole().isServer() || req.isForce()) {
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
    } else if (strategy.acceptsPull() || response.isForce()) {
      taskManager.execute(new ShellScriptListResponseHandlerTask(response, pullProcessState), null);
    } else {
      logger().debug("Ignoring " + response + "; repo type is " + serverContext().getCorusHost().getRepoRole());
    }
  }

  void handleShellScriptDeploymentRequest(ShellScriptDeploymentRequest req) {
    if (serverContext().getCorusHost().getRepoRole().isServer() || req.isForce()) {
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
    } else if (strategy.acceptsPull() || response.isForce()) {
      taskManager.execute(new FileListResponseHandlerTask(response, pullProcessState), null);
    } else {
      logger().debug("Ignoring " + response + "; repo type is " + serverContext().getCorusHost().getRepoRole());
    }
  }

  void handleFileDeploymentRequest(FileDeploymentRequest req) {
    if (serverContext().getCorusHost().getRepoRole().isServer() || req.isForce()) {
      deployRequests.add(req);
      taskManager.execute(new ArtifactDeploymentRequestHandlerTask(repoConfig, deployRequests), null);
    } else {
      logger().debug("Ignoring " + req + "; repo type is " + serverContext().getCorusHost().getRepoRole());
    }
  }
  
  // visible for testing
  void initStrategy() {
    if (this.repoConfig.isRepoServerSyncEnabled() && serverContext().getCorusHost().getRepoRole() == RepoRole.SERVER) {
      strategy = new RepoServerSyncStrategy(serverContext().getCorusHost().getRepoRole());
    } else {
      strategy = new DefaultRepoStrategy(serverContext().getCorusHost().getRepoRole());
    }
  }
  
  RepoStrategy getStrategy() {
    return strategy;
  }
  
  void startCheckNodeTask() {
    synchronized (lock) {
      if (!checkNodeStateTaskStarted && repoConfig.isCheckStateEnabled()) {
        CheckNodeStateTask   check;
        
        if (repoConfig.isCheckStateAutomatic()) {
          if (serverContext().getCorusHost().getRepoRole() == RepoRole.SERVER) {
            logger().info("State checking is set to automatic mode and node is repo server: "
                + "will not copy state from random peers");
            check = new CheckNodeStateTask(
                state,
                pullProcessState, 
                0, 
                false
            );          
          } else {
            logger().info("State checking is set to automatic mode and node is NOT a repo server: "
                + "will copy state from random peers if no repo server nodes are found");
            check = new CheckNodeStateTask(
                state,
                pullProcessState, 
                repoConfig.getCheckStateMaxRandomHosts(), 
                true
            );    
          }
        } else {
          logger().info("State checking is NOT set to automatic mode. Will used explicit configuration params:");
          logger().info("  -> Max random hosts to copy state from (if repo server nodes are not found): " 
              + repoConfig.getCheckStateMaxRandomHosts());
          logger().info("  -> Random host lookup enabled: " + repoConfig.isCheckStateRandomHostsEnabled());         
          
          check = new CheckNodeStateTask(
              state,
              pullProcessState, 
              repoConfig.getCheckStateMaxRandomHosts(), 
              repoConfig.isCheckStateRandomHostsEnabled()
          );
        }
        long checkInterval = TimeValue.createSeconds(repoConfig.getCheckStateIntervalSeconds()).getValueInMillis();
        BackgroundTaskConfig config = BackgroundTaskConfig.create()
            .setExecDelay(checkInterval)
            .setExecInterval(checkInterval);
        taskManager.executeBackground(check, null, config);
        checkNodeStateTaskStarted = true;
      } else if (!repoConfig.isCheckStateEnabled()) {
        logger().warn("State checking is disabled");
      }
    }
  }

}