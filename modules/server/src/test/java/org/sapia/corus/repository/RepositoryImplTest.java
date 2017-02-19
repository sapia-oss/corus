package org.sapia.corus.repository;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.cluster.ClusterNotification;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.cluster.event.CorusHostAddEvent;
import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.configurator.Property;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.FileInfo;
import org.sapia.corus.client.services.deployer.ShellScript;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.processor.ExecConfig;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.client.services.repository.ArtifactDeploymentRequest;
import org.sapia.corus.client.services.repository.ArtifactListRequest;
import org.sapia.corus.client.services.repository.ConfigNotification;
import org.sapia.corus.client.services.repository.DistributionDeploymentRequest;
import org.sapia.corus.client.services.repository.DistributionListResponse;
import org.sapia.corus.client.services.repository.ExecConfigNotification;
import org.sapia.corus.client.services.repository.FileDeploymentRequest;
import org.sapia.corus.client.services.repository.FileListResponse;
import org.sapia.corus.client.services.repository.SecurityConfigNotification;
import org.sapia.corus.client.services.repository.ShellScriptDeploymentRequest;
import org.sapia.corus.client.services.repository.ShellScriptListResponse;
import org.sapia.corus.client.services.security.ApplicationKeyManager;
import org.sapia.corus.client.services.security.ApplicationKeyManager.AppKeyConfig;
import org.sapia.corus.client.services.security.Permission;
import org.sapia.corus.client.services.security.SecurityModule;
import org.sapia.corus.client.services.security.SecurityModule.RoleConfig;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.taskmanager.core.BackgroundTaskConfig;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskManager;
import org.sapia.corus.util.DelayedQueue;
import org.sapia.corus.util.Queue;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.rmi.server.transport.socket.TcpSocketAddress;
import org.sapia.ubik.util.Collects;
import org.springframework.context.ApplicationContext;

public class RepositoryImplTest {
  
  private ClusterManager        cluster;
  private Configurator          config;
  private EventDispatcher       dispatcher;
  private Processor             processor;
  private TaskManager           tasks;
  private SecurityModule        security;
  private Deployer              deployer;
  private ApplicationKeyManager appkeys;
  private ApplicationContext    appCtx;
  private ServerContext         serverCtx;
  private CorusHost             host;
  private RepositoryImpl        repo;
  private Set<CorusHost>        peers;
  private Queue<ArtifactListRequest>       listRequestQueue;
  private DelayedQueue<ArtifactDeploymentRequest> deployRequestQueue;
  private RepositoryConfigurationImpl      repoConfig;
  private int                              corusPort;
  @Before
  public void setUp() throws Exception {
    repoConfig = new RepositoryConfigurationImpl();
    repo = new RepositoryImpl();
    repo.setRepoConfig(repoConfig);

    cluster    = mock(ClusterManager.class);
    config     = mock(Configurator.class);
    dispatcher = mock(EventDispatcher.class);
    processor  = mock(Processor.class);
    tasks      = mock(TaskManager.class);
    security   = mock(SecurityModule.class);
    deployer   = mock(Deployer.class);
    appkeys    = mock(ApplicationKeyManager.class);
    appCtx     = mock(ApplicationContext.class);
    serverCtx  = mock(ServerContext.class);
    listRequestQueue   = mock(Queue.class);
    deployRequestQueue = mock(DelayedQueue.class);
    
    host       = createCorusHost(RepoRole.SERVER);
    
    peers = Collects.arrayToSet(
        createCorusHost(RepoRole.CLIENT), 
        createCorusHost(RepoRole.CLIENT)
    );
    
    repo.setClusterManager(cluster);
    repo.setConfigurator(config);
    repo.setDispatcher(dispatcher);
    repo.setTaskManager(tasks);
    repo.setSecurityModule(security);
    repo.setDeployer(deployer);
    repo.setApplicationKeys(appkeys);
    repo.setApplicationContext(appCtx);
    repo.setServerContext(serverCtx);
    repo.setDeployRequestQueue(deployRequestQueue);
    repo.setArtifactListRequestQueue(listRequestQueue);
    repo.setRepoStrategy(new DefaultRepoStrategy(host.getRepoRole()));
    
    when(serverCtx.getCorusHost()).thenReturn(host);
    when(cluster.getHosts()).thenReturn(peers);
  }
  
  private CorusHost createCorusHost(RepoRole repoRole) {
    CorusHost host = CorusHost.newInstance("test-node", new Endpoint(new TcpSocketAddress("test", corusPort++), new TcpSocketAddress("test", corusPort++)), "testOsInfo", "testVMInfo", mock(PublicKey.class));
    host.setRepoRole(repoRole);
    return host;
  }
  
  @Test
  public void testPullForClientNode() {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    repo.pull();
    verify(tasks).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class)); 
  }
  
  @Test
  public void testPullForServerNode() {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    repo.pull();
    verify(tasks, never()).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class)); 
  }
  
  @Test
  public void testPullForServerNode_server_sync() {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new RepoServerSyncStrategy(RepoRole.SERVER));
    repo.pull();
    verify(tasks).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class)); 
  }

  @Test
  public void testPullForUndefinedNode() {
    host.setRepoRole(RepoRole.NONE);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.NONE));
    repo.pull();
    verify(tasks, never()).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class)); 
  }
  
  @Test
  public void testPushForClientNode() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    repo.push();
    verify(cluster, never()).send(any(ClusterNotification.class)); 
  }
  
  public void testPushForServerNode() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    repo.push();
    verify(cluster).send(any(ClusterNotification.class)); 
  }

  @Test
  public void testPushForUndefinedNode() throws Exception {
    host.setRepoRole(RepoRole.NONE);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.NONE));
    repo.push();
    verify(cluster, never()).send(any(ClusterNotification.class)); 
  }
  
  @Test
  public void testHandleArtifactListRequest() throws Exception {
    ArtifactListRequest req = new ArtifactListRequest(createCorusHost(RepoRole.CLIENT).getEndpoint());
    RemoteEvent event = new RemoteEvent(ArtifactListRequest.EVENT_TYPE, req);
    repo.onAsyncEvent(event);
    verify(listRequestQueue).add(any(ArtifactListRequest.class));
  }
  
  // --------------------------------------------------------------------------
  // Distributions
  
  @Test
  public void testHandleDistributionListResponse() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    DistributionListResponse res = new DistributionListResponse(createCorusHost(RepoRole.SERVER).getEndpoint());
    RemoteEvent event = new RemoteEvent(DistributionListResponse.EVENT_TYPE, res);
    repo.onAsyncEvent(event);
    verify(tasks).execute(any(Task.class), any(Void.class));
  }

  @Test
  public void testHandleDistributionListResponse_no_server_sync() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    DistributionListResponse res = new DistributionListResponse(createCorusHost(RepoRole.SERVER).getEndpoint());
    RemoteEvent event = new RemoteEvent(DistributionListResponse.EVENT_TYPE, res);
    repo.onAsyncEvent(event);
    verify(tasks, never()).execute(any(Task.class), any(Void.class));
  }
  
  @Test
  public void testHandleDistributionListResponse_server_sync() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new RepoServerSyncStrategy(RepoRole.SERVER));
    DistributionListResponse res = new DistributionListResponse(createCorusHost(RepoRole.SERVER).getEndpoint());
    RemoteEvent event = new RemoteEvent(DistributionListResponse.EVENT_TYPE, res);
    repo.onAsyncEvent(event);
    verify(tasks).execute(any(Task.class), any(Void.class));
  }
  
  @Test
  public void testHandleDistributionDeploymentRequest() throws Exception {
    DistributionDeploymentRequest req = new DistributionDeploymentRequest(createCorusHost(RepoRole.CLIENT).getEndpoint());
    RemoteEvent event = new RemoteEvent(DistributionDeploymentRequest.EVENT_TYPE, req);
    repo.onAsyncEvent(event);
    verify(deployRequestQueue).add(any(DistributionDeploymentRequest.class));
  }
  
  // --------------------------------------------------------------------------
  // ShellScripts
  
  @Test
  public void testHandleShellScriptListResponse() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    List<ShellScript> scripts = new ArrayList<ShellScript>();
    ShellScriptListResponse res = new ShellScriptListResponse(createCorusHost(RepoRole.SERVER).getEndpoint(), scripts);
    RemoteEvent event = new RemoteEvent(ShellScriptListResponse.EVENT_TYPE, res);
    repo.onAsyncEvent(event);
    verify(tasks).execute(any(Task.class), any(Void.class));
  }
  
  @Test
  public void testHandleShellScriptListResponse_server_sync() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new RepoServerSyncStrategy(RepoRole.SERVER));
    List<ShellScript> scripts = new ArrayList<ShellScript>();
    ShellScriptListResponse res = new ShellScriptListResponse(createCorusHost(RepoRole.SERVER).getEndpoint(), scripts);
    RemoteEvent event = new RemoteEvent(ShellScriptListResponse.EVENT_TYPE, res);
    repo.onAsyncEvent(event);
    verify(tasks).execute(any(Task.class), any(Void.class));
  }
  
  @Test
  public void testHandleShellScriptListResponse_no_server_sync() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    List<ShellScript> scripts = new ArrayList<ShellScript>();
    ShellScriptListResponse res = new ShellScriptListResponse(createCorusHost(RepoRole.SERVER).getEndpoint(), scripts);
    RemoteEvent event = new RemoteEvent(ShellScriptListResponse.EVENT_TYPE, res);
    repo.onAsyncEvent(event);
    verify(tasks, never()).execute(any(Task.class), any(Void.class));
  }

  @Test
  public void testHandleShellScriptListResponsePullDisabled() throws Exception {
    repoConfig.setPullScriptsEnabled(false);
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    List<ShellScript> scripts = new ArrayList<ShellScript>();
    ShellScriptListResponse res = new ShellScriptListResponse(createCorusHost(RepoRole.SERVER).getEndpoint(), scripts);
    RemoteEvent event = new RemoteEvent(ShellScriptListResponse.EVENT_TYPE, res);
    repo.onAsyncEvent(event);
    verify(tasks, never()).execute(any(Task.class), any(Void.class));
  }
  
  @Test
  public void testHandleShellScriptDeploymentRequest() throws Exception {
    List<ShellScript> scripts = new ArrayList<ShellScript>();
    ShellScriptDeploymentRequest req = new ShellScriptDeploymentRequest(createCorusHost(RepoRole.CLIENT).getEndpoint(), scripts);
    RemoteEvent event = new RemoteEvent(ShellScriptDeploymentRequest.EVENT_TYPE, req);
    repo.onAsyncEvent(event);
    verify(deployRequestQueue).add(any(ShellScriptDeploymentRequest.class));
  }
  
  // --------------------------------------------------------------------------
  // Files
  
  @Test
  public void testHandleFileListResponse() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    List<FileInfo> files = new ArrayList<FileInfo>();
    FileListResponse res = new FileListResponse(createCorusHost(RepoRole.SERVER).getEndpoint(), files);
    RemoteEvent event = new RemoteEvent(FileListResponse.EVENT_TYPE, res);
    repo.onAsyncEvent(event);
    verify(tasks).execute(any(Task.class), any(Void.class));
  }
  
  @Test
  public void testHandleFileListResponse_server_sync() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new RepoServerSyncStrategy(RepoRole.SERVER));
    List<FileInfo> files = new ArrayList<FileInfo>();
    FileListResponse res = new FileListResponse(createCorusHost(RepoRole.SERVER).getEndpoint(), files);
    RemoteEvent event = new RemoteEvent(FileListResponse.EVENT_TYPE, res);
    repo.onAsyncEvent(event);
    verify(tasks).execute(any(Task.class), any(Void.class));
  }
  
  @Test
  public void testHandleFileListResponse_no_server_sync() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    List<FileInfo> files = new ArrayList<FileInfo>();
    FileListResponse res = new FileListResponse(createCorusHost(RepoRole.SERVER).getEndpoint(), files);
    RemoteEvent event = new RemoteEvent(FileListResponse.EVENT_TYPE, res);
    repo.onAsyncEvent(event);
    verify(tasks, never()).execute(any(Task.class), any(Void.class));
  }
  
  @Test
  public void testHandleFileListResponsePullDisabled() throws Exception {
    repoConfig.setPullFilesEnabled(false);    
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    List<FileInfo> files = new ArrayList<FileInfo>();
    FileListResponse res = new FileListResponse(createCorusHost(RepoRole.SERVER).getEndpoint(), files);
    RemoteEvent event = new RemoteEvent(FileListResponse.EVENT_TYPE, res);
    repo.onAsyncEvent(event);
    verify(tasks, never()).execute(any(Task.class), any(Void.class));
  }
  
  @Test
  public void testHandleFileDeploymentRequest() throws Exception {
    List<FileInfo> files = new ArrayList<FileInfo>();
    FileDeploymentRequest req = new FileDeploymentRequest(createCorusHost(RepoRole.CLIENT).getEndpoint(), files);
    RemoteEvent event = new RemoteEvent(FileDeploymentRequest.EVENT_TYPE, req);
    repo.onAsyncEvent(event);
    verify(deployRequestQueue).add(any(FileDeploymentRequest.class));
  }

  // --------------------------------------------------------------------------
  // ExecConfigs
  
  @Test
  public void testHandleExecConfigNotification() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    ExecConfig conf = new ExecConfig();
    conf.setName("test");
    conf.setStartOnBoot(true);
    ExecConfigNotification notif = new ExecConfigNotification(Collects.arrayToList(conf));
    notif.addTarget(host.getEndpoint());
    
    RemoteEvent event = new RemoteEvent(ExecConfigNotification.EVENT_TYPE, notif);
    repo.onSyncEvent(event);

    verify(tasks).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class));
    verify(cluster).send(any(ExecConfigNotification.class));
  }
  
  @Test
  public void testHandleExecConfigNotification_server_sync() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new RepoServerSyncStrategy(RepoRole.SERVER));
    ExecConfig conf = new ExecConfig();
    conf.setName("test");
    conf.setStartOnBoot(true);
    ExecConfigNotification notif = new ExecConfigNotification(Collects.arrayToList(conf));
    notif.addTarget(host.getEndpoint());
    
    RemoteEvent event = new RemoteEvent(ExecConfigNotification.EVENT_TYPE, notif);
    repo.onSyncEvent(event);

    verify(tasks).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class));
    verify(cluster).send(any(ExecConfigNotification.class));
  }
  
  @Test
  public void testHandleExecConfigNotification_no_server_sync() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    ExecConfig conf = new ExecConfig();
    conf.setName("test");
    conf.setStartOnBoot(true);
    ExecConfigNotification notif = new ExecConfigNotification(Collects.arrayToList(conf));
    notif.addTarget(host.getEndpoint());
    
    RemoteEvent event = new RemoteEvent(ExecConfigNotification.EVENT_TYPE, notif);
    repo.onSyncEvent(event);

    verify(tasks, never()).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class));
    verify(cluster, never()).send(any(ExecConfigNotification.class));
  }
  
  @Test
  public void testHandleExecConfigNotificationHostNotTargeted() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    ExecConfig conf = new ExecConfig();
    conf.setName("test");
    ExecConfigNotification notif = new ExecConfigNotification(Collects.arrayToList(conf));
    notif.addTarget(createCorusHost(RepoRole.CLIENT).getEndpoint());
    
    RemoteEvent event = new RemoteEvent(ExecConfigNotification.EVENT_TYPE, notif);
    repo.onSyncEvent(event);
    
    verify(tasks, never()).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class));
    verify(cluster).send(any(ExecConfigNotification.class));
  }
  
  // --------------------------------------------------------------------------
  // Configs
  
  @Test
  public void testHandleConfigNotification() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    ConfigNotification notif = new ConfigNotification();
    notif.addTarget(host.getEndpoint());
    
    Properties props = new Properties();
    props.setProperty("test", "val");
    notif.addProperties(Collects.arrayToList(new Property("test", "val")));
    notif.addTags(Collects.arrayToSet("tag1", "tag2"));
    
    RemoteEvent event = new RemoteEvent(ConfigNotification.EVENT_TYPE, notif);
    repo.onSyncEvent(event);
    
    verify(config).addProperty(eq(PropertyScope.PROCESS), eq("test"), eq("val"), eq(new HashSet<String>()));
    verify(config).addTags(anySet(), eq(false));
    
    verify(cluster).send(any(ExecConfigNotification.class));
  }
  
  @Test
  public void testHandleConfigNotification_server_sync() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new RepoServerSyncStrategy(RepoRole.SERVER));
    ConfigNotification notif = new ConfigNotification();
    notif.addTarget(host.getEndpoint());
    
    Properties props = new Properties();
    props.setProperty("test", "val");
    notif.addProperties(Collects.arrayToList(new Property("test", "val")));
    notif.addTags(Collects.arrayToSet("tag1", "tag2"));
    
    RemoteEvent event = new RemoteEvent(ConfigNotification.EVENT_TYPE, notif);
    repo.onSyncEvent(event);
    
    verify(config).addProperty(eq(PropertyScope.PROCESS), eq("test"), eq("val"), eq(new HashSet<String>()));
    verify(config).addTags(anySet(), eq(false));
    
    verify(cluster).send(any(ExecConfigNotification.class));
  }
  
  @Test
  public void testHandleConfigNotification_no_server_sync() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    ConfigNotification notif = new ConfigNotification();
    notif.addTarget(host.getEndpoint());
    
    Properties props = new Properties();
    props.setProperty("test", "val");
    notif.addProperties(Collects.arrayToList(new Property("test", "val")));
    notif.addTags(Collects.arrayToSet("tag1", "tag2"));
    
    RemoteEvent event = new RemoteEvent(ConfigNotification.EVENT_TYPE, notif);
    repo.onSyncEvent(event);
    
    verify(config, never()).addProperty(eq(PropertyScope.PROCESS), eq("test"), eq("val"), eq(new HashSet<String>()));
    verify(config, never()).addTags(anySet(), eq(false));
    
    verify(cluster, never()).send(any(ExecConfigNotification.class));
  }
  
  @Test
  public void testHandleConfigNotificationTagsPullDisabled() throws Exception {
    repoConfig.setPullTagsEnabled(false);
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    ConfigNotification notif = new ConfigNotification();
    notif.addTarget(host.getEndpoint());
    
    Properties props = new Properties();
    props.setProperty("test", "val");
    notif.addProperties(Collects.arrayToList(new Property("test", "val")));
    notif.addTags(Collects.arrayToSet("tag1", "tag2"));
    
    RemoteEvent event = new RemoteEvent(ConfigNotification.EVENT_TYPE, notif);
    repo.onSyncEvent(event);
    
    verify(config).addProperty(eq(PropertyScope.PROCESS), eq("test"), eq("val"), eq(new HashSet<String>()));
    verify(config, never()).addTags(anySet(), eq(false));
    
    verify(cluster).send(any(ExecConfigNotification.class));
  }
  
  @Test
  public void testHandleConfigNotificationPropertiesPullDisabled() throws Exception {
    repoConfig.setPullPropertiesEnabled(false);
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    ConfigNotification notif = new ConfigNotification();
    notif.addTarget(host.getEndpoint());
    
    Properties props = new Properties();
    props.setProperty("test", "val");
    notif.addProperties(Collects.arrayToList(new Property("test", "val")));
    notif.addTags(Collects.arrayToSet("tag1", "tag2"));
    
    RemoteEvent event = new RemoteEvent(ConfigNotification.EVENT_TYPE, notif);
    repo.onSyncEvent(event);
    
    verify(config, never()).addProperty(eq(PropertyScope.PROCESS), eq("test"), eq("val"), eq(new HashSet<String>()));
    verify(config).addTags(anySet(), eq(false));
    
    verify(cluster).send(any(ExecConfigNotification.class));
  }  
  
  @Test
  public void testConfigNotificationHostNotTargeted() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    ConfigNotification notif = new ConfigNotification();
    notif.addTarget(createCorusHost(RepoRole.CLIENT).getEndpoint());
    
    Properties props = new Properties();
    props.setProperty("test", "val");
    notif.addProperties(Collects.arrayToList(new Property("test", "val")));
    notif.addTags(Collects.arrayToSet("tag1", "tag2"));
    
    RemoteEvent event = new RemoteEvent(ConfigNotification.EVENT_TYPE, notif);
    repo.onSyncEvent(event);
    
    verify(config, never()).addProperty(eq(PropertyScope.PROCESS), eq("test"), eq("val"), eq(new HashSet<String>()));
    verify(config, never()).addTags(anySet(), eq(false));
    
    verify(cluster).send(any(ExecConfigNotification.class));

  }
  
  // --------------------------------------------------------------------------
  // Security
  
  @Test
  public void testHandleSecurityConfigNotification() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    List<RoleConfig>   roles = Collects.arrayToList(new RoleConfig("admin", Collects.arrayToSet(Permission.values())));
    List<AppKeyConfig> keys  = Collects.arrayToList(new AppKeyConfig("test-app", "test-role", "test-key"));
    SecurityConfigNotification notif = new SecurityConfigNotification(roles, keys);
    notif.addTarget(host.getEndpoint());
    
    RemoteEvent event = new RemoteEvent(SecurityConfigNotification.EVENT_TYPE, notif);
    repo.onSyncEvent(event);
    
    verify(security).addOrUpdateRole(eq("admin"), anySetOf(Permission.class));
    verify(appkeys).addOrUpdateApplicationKey(eq("test-app"), eq("test-key"), eq("test-role"));
    verify(cluster).send(any(SecurityConfigNotification.class));
  }
  
  @Test
  public void testHandleSecurityConfigNotification_server_sync() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new RepoServerSyncStrategy(RepoRole.SERVER));
    List<RoleConfig>   roles = Collects.arrayToList(new RoleConfig("admin", Collects.arrayToSet(Permission.values())));
    List<AppKeyConfig> keys  = Collects.arrayToList(new AppKeyConfig("test-app", "test-role", "test-key"));
    SecurityConfigNotification notif = new SecurityConfigNotification(roles, keys);
    notif.addTarget(host.getEndpoint());
    
    RemoteEvent event = new RemoteEvent(SecurityConfigNotification.EVENT_TYPE, notif);
    repo.onSyncEvent(event);
    
    verify(security).addOrUpdateRole(eq("admin"), anySetOf(Permission.class));
    verify(appkeys).addOrUpdateApplicationKey(eq("test-app"), eq("test-key"), eq("test-role"));
    verify(cluster).send(any(SecurityConfigNotification.class));
  }

  @Test
  public void testHandleSecurityConfigNotification_no_server_sync() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    List<RoleConfig>   roles = Collects.arrayToList(new RoleConfig("admin", Collects.arrayToSet(Permission.values())));
    List<AppKeyConfig> keys  = Collects.arrayToList(new AppKeyConfig("test-app", "test-role", "test-key"));
    SecurityConfigNotification notif = new SecurityConfigNotification(roles, keys);
    notif.addTarget(host.getEndpoint());
    
    RemoteEvent event = new RemoteEvent(SecurityConfigNotification.EVENT_TYPE, notif);
    repo.onSyncEvent(event);
    
    verify(security, never()).addOrUpdateRole(eq("admin"), anySetOf(Permission.class));
    verify(appkeys, never()).addOrUpdateApplicationKey(eq("test-app"), eq("test-key"), eq("test-role"));
    verify(cluster, never()).send(any(SecurityConfigNotification.class));
  }
  
  @Test
  public void testHandleSecurityConfigPullDisabled() throws Exception {
    repoConfig.setPullSecurityConfigEnabled(false);
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    List<RoleConfig>   roles = Collects.arrayToList(new RoleConfig("admin", Collects.arrayToSet(Permission.values())));
    List<AppKeyConfig> keys  = Collects.arrayToList(new AppKeyConfig("test-app", "test-role", "test-key"));
    SecurityConfigNotification notif = new SecurityConfigNotification(roles, keys);
    notif.addTarget(host.getEndpoint());
    
    RemoteEvent event = new RemoteEvent(SecurityConfigNotification.EVENT_TYPE, notif);
    repo.onSyncEvent(event);
    
    verify(security, never()).addOrUpdateRole(eq("admin"), anySetOf(Permission.class));
    verify(appkeys, never()).addOrUpdateApplicationKey(eq("test-app"), eq("test-key"), eq("test-role"));
    verify(cluster).send(any(SecurityConfigNotification.class));
  }
  
  @Test
  public void testSecurityConfigNotificationHostNotTargeted() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    List<RoleConfig>   roles = Collects.arrayToList(new RoleConfig("admin", Collects.arrayToSet(Permission.values())));
    List<AppKeyConfig> keys  = Collects.arrayToList(new AppKeyConfig("test-app", "test-role", "test-key"));
    SecurityConfigNotification notif = new SecurityConfigNotification(roles, keys);
    notif.addTarget(createCorusHost(RepoRole.CLIENT).getEndpoint());
    
    RemoteEvent event = new RemoteEvent(SecurityConfigNotification.EVENT_TYPE, notif);
    repo.onSyncEvent(event);
    
    verify(security, never()).addOrUpdateRole(eq("admin"), anySetOf(Permission.class));
    verify(appkeys, never()).addOrUpdateApplicationKey(eq("test-app"), eq("test-key"), eq("test-role"));
    verify(cluster).send(any(SecurityConfigNotification.class));
  }
  
  @Test
  public void testOnCorusHostAddEvent_client() {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
      
    repo.onCorusHostAddEvent(new CorusHostAddEvent(createCorusHost(RepoRole.SERVER)));
    
    verify(tasks).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class));
  }
  
  @Test
  public void testOnCorusHostAddEvent_client_has_distributions() {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
      
    when(deployer.getDistributions(any())).thenReturn(Arrays.asList(new Distribution("test", "1.0")));

    repo.onCorusHostAddEvent(new CorusHostAddEvent(createCorusHost(RepoRole.SERVER)));
    
    verify(tasks, never()).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class));
  }
  
  @Test
  public void testOnCorusHostAddEvent_new_node_is_client() {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
  
    repo.onCorusHostAddEvent(new CorusHostAddEvent(createCorusHost(RepoRole.CLIENT)));
    
    verify(tasks, never()).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class));
  }

  @Test
  public void testOnCorusHostAddEvent_server_sync() {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new RepoServerSyncStrategy(RepoRole.SERVER));
    
    repo.onCorusHostAddEvent(new CorusHostAddEvent(createCorusHost(RepoRole.SERVER)));
    
    verify(tasks).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class));
  }
  
  @Test
  public void testOnCorusHostAddEvent_server_sync_has_distributions() {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new RepoServerSyncStrategy(RepoRole.SERVER));

    when(deployer.getDistributions(any())).thenReturn(Arrays.asList(new Distribution("test", "1.0")));

    repo.onCorusHostAddEvent(new CorusHostAddEvent(createCorusHost(RepoRole.SERVER)));
    
    verify(tasks, never()).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class));
  }
  
  @Test
  public void testOnCorusHostAddEvent_no_server_sync() {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));

    repo.onCorusHostAddEvent(new CorusHostAddEvent(createCorusHost(RepoRole.SERVER)));
    
    verify(tasks, never()).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class));
  }
  
  @Test
  public void testInitStrategy_client() {
    host.setRepoRole(RepoRole.CLIENT);
    
    repo.initStrategy();
    
    assertTrue(repo.getStrategy() instanceof DefaultRepoStrategy);
  }
  
  @Test
  public void testInitStrategy_server_sync() {
    host.setRepoRole(RepoRole.SERVER);
    this.repoConfig.setRepoServerSyncEnabled(true);
    
    repo.initStrategy();
    
    assertTrue(repo.getStrategy() instanceof RepoServerSyncStrategy);
  }
  
  @Test
  public void testInitStrategy_no_server_sync() {
    host.setRepoRole(RepoRole.SERVER);
    
    repo.initStrategy();
    
    assertTrue(repo.getStrategy() instanceof DefaultRepoStrategy);
  }
  
  @Test
  public void testChangeRole_to_server_sync() {
    host.setRepoRole(RepoRole.CLIENT);
    repoConfig.setRepoServerSyncEnabled(true);
    
    repo.changeRole(RepoRole.SERVER);
    
    assertTrue(repo.getStrategy() instanceof RepoServerSyncStrategy);
  }
}
