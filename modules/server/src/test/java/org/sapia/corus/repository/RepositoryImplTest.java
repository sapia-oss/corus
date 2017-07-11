package org.sapia.corus.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import org.mockito.ArgumentCaptor;
import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.cluster.ClusterNotification;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.cluster.event.CorusHostAddedEvent;
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
import org.sapia.corus.repository.task.CheckNodeStateTask;
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
  public void testPull_for_client() {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    repo.pull();
    verify(tasks).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class)); 
  }
  
  @Test
  public void testPull_for_server() {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    repo.pull();
    verify(tasks, never()).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class)); 
  }
  
  @Test
  public void testPull_for_server_sync() {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new RepoServerSyncStrategy(RepoRole.SERVER));
    repo.pull();
    verify(tasks).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class)); 
  }

  @Test
  public void testPull_for_undefined_node() {
    host.setRepoRole(RepoRole.NONE);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.NONE));
    repo.pull();
    verify(tasks, never()).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class)); 
  }
  
  @Test
  public void testPush_for_client() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    repo.push();
    verify(cluster, never()).dispatch(any(ClusterNotification.class)); 
  }
  
  public void testPush_for_server_node() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    repo.push();
    verify(cluster).dispatch(any(ClusterNotification.class)); 
  }

  @Test
  public void testPush_for_undefined_node() throws Exception {
    host.setRepoRole(RepoRole.NONE);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.NONE));
    repo.push();
    verify(cluster, never()).dispatch(any(ClusterNotification.class)); 
  }
  
  @Test
  public void testHandleArtifactListRequest_for_server() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    ArtifactListRequest req = new ArtifactListRequest(createCorusHost(RepoRole.CLIENT).getEndpoint());
    RemoteEvent event = new RemoteEvent(ArtifactListRequest.EVENT_TYPE, req);
    repo.onAsyncEvent(event);
    verify(listRequestQueue).add(any(ArtifactListRequest.class));
  }
  
  @Test
  public void testHandleArtifactListRequest_for_client() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    ArtifactListRequest req = new ArtifactListRequest(createCorusHost(RepoRole.CLIENT).getEndpoint());
    RemoteEvent event = new RemoteEvent(ArtifactListRequest.EVENT_TYPE, req);
    repo.onAsyncEvent(event);
    verify(listRequestQueue, never()).add(any(ArtifactListRequest.class));
  }
  
  @Test
  public void testHandleArtifactListRequest_for_client_force() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    ArtifactListRequest req = new ArtifactListRequest(createCorusHost(RepoRole.CLIENT).getEndpoint());
    req.setForce(true);
    RemoteEvent event = new RemoteEvent(ArtifactListRequest.EVENT_TYPE, req);
    repo.onAsyncEvent(event);
    verify(listRequestQueue).add(any(ArtifactListRequest.class));
  }
  
  // --------------------------------------------------------------------------
  // Distributions
  
  @Test
  public void testHandleDistributionListResponse_for_client() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    DistributionListResponse res = new DistributionListResponse(createCorusHost(RepoRole.SERVER).getEndpoint());
    RemoteEvent event = new RemoteEvent(DistributionListResponse.EVENT_TYPE, res);
    repo.onAsyncEvent(event);
    verify(tasks).execute(any(Task.class), any(Void.class));
  }

  @Test
  public void testHandleDistributionListResponse_for_server_no_sync() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    DistributionListResponse res = new DistributionListResponse(createCorusHost(RepoRole.SERVER).getEndpoint());
    RemoteEvent event = new RemoteEvent(DistributionListResponse.EVENT_TYPE, res);
    repo.onAsyncEvent(event);
    verify(tasks, never()).execute(any(Task.class), any(Void.class));
  }
  
  @Test
  public void testHandleDistributionListResponse_for_server_with_sync() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new RepoServerSyncStrategy(RepoRole.SERVER));
    DistributionListResponse res = new DistributionListResponse(createCorusHost(RepoRole.SERVER).getEndpoint());
    RemoteEvent event = new RemoteEvent(DistributionListResponse.EVENT_TYPE, res);
    repo.onAsyncEvent(event);
    verify(tasks).execute(any(Task.class), any(Void.class));
  }
  
  @Test
  public void testHandleDistributionListResponse_for_server_force() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    DistributionListResponse res = new DistributionListResponse(createCorusHost(RepoRole.SERVER).getEndpoint());
    res.setForce(true);
    RemoteEvent event = new RemoteEvent(DistributionListResponse.EVENT_TYPE, res);
    repo.onAsyncEvent(event);
    verify(tasks).execute(any(Task.class), any(Void.class));
  }
  
  @Test
  public void testHandleDistributionDeploymentRequest_for_server() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    DistributionDeploymentRequest req = new DistributionDeploymentRequest(createCorusHost(RepoRole.CLIENT).getEndpoint());
    RemoteEvent event = new RemoteEvent(DistributionDeploymentRequest.EVENT_TYPE, req);
    repo.onAsyncEvent(event);
    verify(deployRequestQueue).add(any(DistributionDeploymentRequest.class));
  }
  
  @Test
  public void testHandleDistributionDeploymentRequest_for_server_force() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    DistributionDeploymentRequest req = new DistributionDeploymentRequest(createCorusHost(RepoRole.CLIENT).getEndpoint());
    req.setForce(true);
    RemoteEvent event = new RemoteEvent(DistributionDeploymentRequest.EVENT_TYPE, req);
    repo.onAsyncEvent(event);
    verify(deployRequestQueue).add(any(DistributionDeploymentRequest.class));
  }
  
  @Test
  public void testHandleDistributionDeploymentRequest_for_client() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    DistributionDeploymentRequest req = new DistributionDeploymentRequest(createCorusHost(RepoRole.CLIENT).getEndpoint());
    RemoteEvent event = new RemoteEvent(DistributionDeploymentRequest.EVENT_TYPE, req);
    repo.onAsyncEvent(event);
    verify(deployRequestQueue, never()).add(any(DistributionDeploymentRequest.class));
  }

  @Test
  public void testHandleDistributionDeploymentRequest_for_client_force() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    DistributionDeploymentRequest req = new DistributionDeploymentRequest(createCorusHost(RepoRole.CLIENT).getEndpoint());
    RemoteEvent event = new RemoteEvent(DistributionDeploymentRequest.EVENT_TYPE, req);
    repo.onAsyncEvent(event);
    verify(deployRequestQueue, never()).add(any(DistributionDeploymentRequest.class));
  }
  
  // --------------------------------------------------------------------------
  // ShellScripts
  
  @Test
  public void testHandleShellScriptListResponse_for_client() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    List<ShellScript> scripts = new ArrayList<ShellScript>();
    ShellScriptListResponse res = new ShellScriptListResponse(createCorusHost(RepoRole.SERVER).getEndpoint(), scripts);
    RemoteEvent event = new RemoteEvent(ShellScriptListResponse.EVENT_TYPE, res);
    repo.onAsyncEvent(event);
    verify(tasks).execute(any(Task.class), any(Void.class));
  }
  
  @Test
  public void testHandleShellScriptListResponse_for_server_sync() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new RepoServerSyncStrategy(RepoRole.SERVER));
    List<ShellScript> scripts = new ArrayList<ShellScript>();
    ShellScriptListResponse res = new ShellScriptListResponse(createCorusHost(RepoRole.SERVER).getEndpoint(), scripts);
    RemoteEvent event = new RemoteEvent(ShellScriptListResponse.EVENT_TYPE, res);
    repo.onAsyncEvent(event);
    verify(tasks).execute(any(Task.class), any(Void.class));
  }
  
  @Test
  public void testHandleShellScriptListResponse_for_server_no_sync() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    List<ShellScript> scripts = new ArrayList<ShellScript>();
    ShellScriptListResponse res = new ShellScriptListResponse(createCorusHost(RepoRole.SERVER).getEndpoint(), scripts);
    RemoteEvent event = new RemoteEvent(ShellScriptListResponse.EVENT_TYPE, res);
    repo.onAsyncEvent(event);
    verify(tasks, never()).execute(any(Task.class), any(Void.class));
  }
  
  @Test
  public void testHandleShellScriptListResponse_for_server_force() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    List<ShellScript> scripts = new ArrayList<ShellScript>();
    ShellScriptListResponse res = new ShellScriptListResponse(createCorusHost(RepoRole.SERVER).getEndpoint(), scripts);
    res.setForce(true);
    RemoteEvent event = new RemoteEvent(ShellScriptListResponse.EVENT_TYPE, res);
    repo.onAsyncEvent(event);
    verify(tasks).execute(any(Task.class), any(Void.class));
  }
  
  
  @Test
  public void testHandleShellScriptDeploymentRequest_for_server() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    List<ShellScript> scripts = new ArrayList<ShellScript>();
    ShellScriptDeploymentRequest req = new ShellScriptDeploymentRequest(createCorusHost(RepoRole.CLIENT).getEndpoint(), scripts);
    RemoteEvent event = new RemoteEvent(ShellScriptDeploymentRequest.EVENT_TYPE, req);
    repo.onAsyncEvent(event);
    verify(deployRequestQueue).add(any(ShellScriptDeploymentRequest.class));
  }

  @Test
  public void testHandleShellScriptListResponse_for_client_pull_disabled() throws Exception {
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
  public void testHandleShellScriptDeploymentRequest_for_client() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    List<ShellScript> scripts = new ArrayList<ShellScript>();
    ShellScriptDeploymentRequest req = new ShellScriptDeploymentRequest(createCorusHost(RepoRole.CLIENT).getEndpoint(), scripts);
    RemoteEvent event = new RemoteEvent(ShellScriptDeploymentRequest.EVENT_TYPE, req);
    repo.onAsyncEvent(event);
    verify(deployRequestQueue, never()).add(any(ShellScriptDeploymentRequest.class));
  }
  
  @Test
  public void testHandleShellScriptDeploymentRequest_for_client_force() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    List<ShellScript> scripts = new ArrayList<ShellScript>();
    ShellScriptDeploymentRequest req = new ShellScriptDeploymentRequest(createCorusHost(RepoRole.CLIENT).getEndpoint(), scripts);
    req.setForce(true);
    RemoteEvent event = new RemoteEvent(ShellScriptDeploymentRequest.EVENT_TYPE, req);
    repo.onAsyncEvent(event);
    verify(deployRequestQueue).add(any(ShellScriptDeploymentRequest.class));
  }
  
  // --------------------------------------------------------------------------
  // Files
  
  @Test
  public void testHandleFileListResponse_for_client() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    List<FileInfo> files = new ArrayList<FileInfo>();
    FileListResponse res = new FileListResponse(createCorusHost(RepoRole.SERVER).getEndpoint(), files);
    RemoteEvent event = new RemoteEvent(FileListResponse.EVENT_TYPE, res);
    repo.onAsyncEvent(event);
    verify(tasks).execute(any(Task.class), any(Void.class));
  }
  
  @Test
  public void testHandleFileListResponse_for_server_sync() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new RepoServerSyncStrategy(RepoRole.SERVER));
    List<FileInfo> files = new ArrayList<FileInfo>();
    FileListResponse res = new FileListResponse(createCorusHost(RepoRole.SERVER).getEndpoint(), files);
    RemoteEvent event = new RemoteEvent(FileListResponse.EVENT_TYPE, res);
    repo.onAsyncEvent(event);
    verify(tasks).execute(any(Task.class), any(Void.class));
  }
  
  @Test
  public void testHandleFileListResponse_for_server_force() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    List<FileInfo> files = new ArrayList<FileInfo>();
    FileListResponse res = new FileListResponse(createCorusHost(RepoRole.SERVER).getEndpoint(), files);
    res.setForce(true);
    RemoteEvent event = new RemoteEvent(FileListResponse.EVENT_TYPE, res);
    repo.onAsyncEvent(event);
    verify(tasks).execute(any(Task.class), any(Void.class));
  }
  
  @Test
  public void testHandleFileListResponse_for_server_no_sync() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    List<FileInfo> files = new ArrayList<FileInfo>();
    FileListResponse res = new FileListResponse(createCorusHost(RepoRole.SERVER).getEndpoint(), files);
    RemoteEvent event = new RemoteEvent(FileListResponse.EVENT_TYPE, res);
    repo.onAsyncEvent(event);
    verify(tasks, never()).execute(any(Task.class), any(Void.class));
  }
  
  @Test
  public void testHandleFileDeploymentRequest_for_server() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    List<FileInfo> files = new ArrayList<FileInfo>();
    FileDeploymentRequest req = new FileDeploymentRequest(createCorusHost(RepoRole.CLIENT).getEndpoint(), files);
    RemoteEvent event = new RemoteEvent(FileDeploymentRequest.EVENT_TYPE, req);
    repo.onAsyncEvent(event);
    verify(deployRequestQueue).add(any(FileDeploymentRequest.class));
  }
  
  @Test
  public void testHandleFileListResponse_for_client_pull_disabled() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    repoConfig.setPullFilesEnabled(false);    
    List<FileInfo> files = new ArrayList<FileInfo>();
    FileListResponse res = new FileListResponse(createCorusHost(RepoRole.SERVER).getEndpoint(), files);
    RemoteEvent event = new RemoteEvent(FileListResponse.EVENT_TYPE, res);
    repo.onAsyncEvent(event);
    verify(tasks, never()).execute(any(Task.class), any(Void.class));
  }
  
  @Test
  public void testHandleFileDeploymentRequest_for_client() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    List<FileInfo> files = new ArrayList<FileInfo>();
    FileDeploymentRequest req = new FileDeploymentRequest(createCorusHost(RepoRole.CLIENT).getEndpoint(), files);
    RemoteEvent event = new RemoteEvent(FileDeploymentRequest.EVENT_TYPE, req);
    repo.onAsyncEvent(event);
    verify(deployRequestQueue, never()).add(any(FileDeploymentRequest.class));
  }
  
  @Test
  public void testHandleFileDeploymentRequest_for_client_force() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    List<FileInfo> files = new ArrayList<FileInfo>();
    FileDeploymentRequest req = new FileDeploymentRequest(createCorusHost(RepoRole.CLIENT).getEndpoint(), files);
    req.setForce(true);
    RemoteEvent event = new RemoteEvent(FileDeploymentRequest.EVENT_TYPE, req);
    repo.onAsyncEvent(event);
    verify(deployRequestQueue).add(any(FileDeploymentRequest.class));
  }

  // --------------------------------------------------------------------------
  // ExecConfigs
  
  @Test
  public void testHandleExecConfigNotification_for_client() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    ExecConfig conf = new ExecConfig();
    conf.setName("test");
    conf.setStartOnBoot(true);
    ExecConfigNotification notif = new ExecConfigNotification(Collects.arrayToList(conf));
    notif.addTarget(host.getEndpoint());
    
    RemoteEvent event = new RemoteEvent(ExecConfigNotification.EVENT_TYPE, notif);
    repo.onAsyncEvent(event);

    verify(tasks).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class));
    verify(cluster).dispatch(any(ExecConfigNotification.class));
  }
  
  @Test
  public void testHandleExecConfigNotification_for_server_sync() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new RepoServerSyncStrategy(RepoRole.SERVER));
    ExecConfig conf = new ExecConfig();
    conf.setName("test");
    conf.setStartOnBoot(true);
    ExecConfigNotification notif = new ExecConfigNotification(Collects.arrayToList(conf));
    notif.addTarget(host.getEndpoint());
    
    RemoteEvent event = new RemoteEvent(ExecConfigNotification.EVENT_TYPE, notif);
    repo.onAsyncEvent(event);

    verify(tasks).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class));
    verify(cluster).dispatch(any(ExecConfigNotification.class));
  }
  
  @Test
  public void testHandleExecConfigNotification_for_server_no_sync() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    ExecConfig conf = new ExecConfig();
    conf.setName("test");
    conf.setStartOnBoot(true);
    ExecConfigNotification notif = new ExecConfigNotification(Collects.arrayToList(conf));
    notif.addTarget(host.getEndpoint());
    
    RemoteEvent event = new RemoteEvent(ExecConfigNotification.EVENT_TYPE, notif);
    repo.onAsyncEvent(event);

    verify(tasks, never()).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class));
    verify(cluster, never()).dispatch(any(ExecConfigNotification.class));
  }
  
  @Test
  public void testHandleExecConfigNotification_for_server_force() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    ExecConfig conf = new ExecConfig();
    conf.setName("test");
    conf.setStartOnBoot(true);
    ExecConfigNotification notif = new ExecConfigNotification(Collects.arrayToList(conf));
    notif.setForce(true);
    notif.addTarget(host.getEndpoint());
    
    RemoteEvent event = new RemoteEvent(ExecConfigNotification.EVENT_TYPE, notif);
    repo.onAsyncEvent(event);

    verify(tasks).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class));
    verify(cluster).dispatch(any(ExecConfigNotification.class));
  }
  
  @Test
  public void testHandleExecConfigNotification_host_not_targeted() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    ExecConfig conf = new ExecConfig();
    conf.setName("test");
    ExecConfigNotification notif = new ExecConfigNotification(Collects.arrayToList(conf));
    notif.addTarget(createCorusHost(RepoRole.CLIENT).getEndpoint());
    
    RemoteEvent event = new RemoteEvent(ExecConfigNotification.EVENT_TYPE, notif);
    repo.onAsyncEvent(event);
    
    verify(tasks, never()).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class));
    verify(cluster).dispatch(any(ExecConfigNotification.class));
  }
  
  // --------------------------------------------------------------------------
  // Configs
  
  @Test
  public void testHandleConfigNotification_for_client() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    ConfigNotification notif = new ConfigNotification();
    notif.addTarget(host.getEndpoint());
    
    Properties props = new Properties();
    props.setProperty("test", "val");
    notif.addProperties(Collects.arrayToList(new Property("test", "val")));
    notif.addTags(Collects.arrayToSet("tag1", "tag2"));
    
    RemoteEvent event = new RemoteEvent(ConfigNotification.EVENT_TYPE, notif);
    repo.onAsyncEvent(event);
    
    verify(config).addProperty(eq(PropertyScope.PROCESS), eq("test"), eq("val"), eq(new HashSet<String>()));
    verify(config).addTags(anySet(), eq(false));
    
    verify(cluster).dispatch(any(ExecConfigNotification.class));
  }
  
  @Test
  public void testHandleConfigNotification_for_server_sync() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new RepoServerSyncStrategy(RepoRole.SERVER));
    ConfigNotification notif = new ConfigNotification();
    notif.addTarget(host.getEndpoint());
    
    Properties props = new Properties();
    props.setProperty("test", "val");
    notif.addProperties(Collects.arrayToList(new Property("test", "val")));
    notif.addTags(Collects.arrayToSet("tag1", "tag2"));
    
    RemoteEvent event = new RemoteEvent(ConfigNotification.EVENT_TYPE, notif);
    repo.onAsyncEvent(event);
    
    verify(config).addProperty(eq(PropertyScope.PROCESS), eq("test"), eq("val"), eq(new HashSet<String>()));
    verify(config).addTags(anySet(), eq(false));
    
    verify(cluster).dispatch(any(ExecConfigNotification.class));
  }
  
  @Test
  public void testHandleConfigNotification_for_server_no_sync() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    ConfigNotification notif = new ConfigNotification();
    notif.addTarget(host.getEndpoint());
    
    Properties props = new Properties();
    props.setProperty("test", "val");
    notif.addProperties(Collects.arrayToList(new Property("test", "val")));
    notif.addTags(Collects.arrayToSet("tag1", "tag2"));
    
    RemoteEvent event = new RemoteEvent(ConfigNotification.EVENT_TYPE, notif);
    repo.onAsyncEvent(event);
    
    verify(config, never()).addProperty(eq(PropertyScope.PROCESS), eq("test"), eq("val"), eq(new HashSet<String>()));
    verify(config, never()).addTags(anySet(), eq(false));
    
    verify(cluster, never()).dispatch(any(ExecConfigNotification.class));
  }
  
  @Test
  public void testHandleConfigNotification_for_server_force() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    ConfigNotification notif = new ConfigNotification();
    notif.setForce(true);
    notif.addTarget(host.getEndpoint());
    
    Properties props = new Properties();
    props.setProperty("test", "val");
    notif.addProperties(Collects.arrayToList(new Property("test", "val")));
    notif.addTags(Collects.arrayToSet("tag1", "tag2"));
    
    RemoteEvent event = new RemoteEvent(ConfigNotification.EVENT_TYPE, notif);
    repo.onAsyncEvent(event);
    
    verify(config).addProperty(eq(PropertyScope.PROCESS), eq("test"), eq("val"), eq(new HashSet<String>()));
    verify(config).addTags(anySet(), eq(false));
    
    verify(cluster).dispatch(any(ExecConfigNotification.class));
  }
  
  @Test
  public void testHandleConfigNotification_client_tags_pull_disabled() throws Exception {
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
    repo.onAsyncEvent(event);
    
    verify(config).addProperty(eq(PropertyScope.PROCESS), eq("test"), eq("val"), eq(new HashSet<String>()));
    verify(config, never()).addTags(anySet(), eq(false));
    
    verify(cluster).dispatch(any(ExecConfigNotification.class));
  }
  
  @Test
  public void testHandleConfigNotification_properties_pull_disabled() throws Exception {
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
    repo.onAsyncEvent(event);
    
    verify(config, never()).addProperty(eq(PropertyScope.PROCESS), eq("test"), eq("val"), eq(new HashSet<String>()));
    verify(config).addTags(anySet(), eq(false));
    
    verify(cluster).dispatch(any(ExecConfigNotification.class));
  }  
  
  @Test
  public void testConfigNotification_host_not_targeted() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    ConfigNotification notif = new ConfigNotification();
    notif.addTarget(createCorusHost(RepoRole.CLIENT).getEndpoint());
    
    Properties props = new Properties();
    props.setProperty("test", "val");
    notif.addProperties(Collects.arrayToList(new Property("test", "val")));
    notif.addTags(Collects.arrayToSet("tag1", "tag2"));
    
    RemoteEvent event = new RemoteEvent(ConfigNotification.EVENT_TYPE, notif);
    repo.onAsyncEvent(event);
    
    verify(config, never()).addProperty(eq(PropertyScope.PROCESS), eq("test"), eq("val"), eq(new HashSet<String>()));
    verify(config, never()).addTags(anySet(), eq(false));
    
    verify(cluster).dispatch(any(ExecConfigNotification.class));

  }
  
  // --------------------------------------------------------------------------
  // Security
  
  @Test
  public void testHandleSecurityConfigNotification_for_client() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    List<RoleConfig>   roles = Collects.arrayToList(new RoleConfig("admin", Collects.arrayToSet(Permission.values())));
    List<AppKeyConfig> keys  = Collects.arrayToList(new AppKeyConfig("test-app", "test-role", "test-key"));
    SecurityConfigNotification notif = new SecurityConfigNotification(roles, keys);
    notif.addTarget(host.getEndpoint());
    
    RemoteEvent event = new RemoteEvent(SecurityConfigNotification.EVENT_TYPE, notif);
    repo.onAsyncEvent(event);
    
    verify(security).addOrUpdateRole(eq("admin"), anySetOf(Permission.class));
    verify(appkeys).addOrUpdateApplicationKey(eq("test-app"), eq("test-key"), eq("test-role"));
    verify(cluster).dispatch(any(SecurityConfigNotification.class));
  }
  
  @Test
  public void testHandleSecurityConfigNotification_for_server_sync() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new RepoServerSyncStrategy(RepoRole.SERVER));
    List<RoleConfig>   roles = Collects.arrayToList(new RoleConfig("admin", Collects.arrayToSet(Permission.values())));
    List<AppKeyConfig> keys  = Collects.arrayToList(new AppKeyConfig("test-app", "test-role", "test-key"));
    SecurityConfigNotification notif = new SecurityConfigNotification(roles, keys);
    notif.addTarget(host.getEndpoint());
    
    RemoteEvent event = new RemoteEvent(SecurityConfigNotification.EVENT_TYPE, notif);
    repo.onAsyncEvent(event);
    
    verify(security).addOrUpdateRole(eq("admin"), anySetOf(Permission.class));
    verify(appkeys).addOrUpdateApplicationKey(eq("test-app"), eq("test-key"), eq("test-role"));
    verify(cluster).dispatch(any(SecurityConfigNotification.class));
  }

  @Test
  public void testHandleSecurityConfigNotification_for_server_no_sync() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    List<RoleConfig>   roles = Collects.arrayToList(new RoleConfig("admin", Collects.arrayToSet(Permission.values())));
    List<AppKeyConfig> keys  = Collects.arrayToList(new AppKeyConfig("test-app", "test-role", "test-key"));
    SecurityConfigNotification notif = new SecurityConfigNotification(roles, keys);
    notif.addTarget(host.getEndpoint());
    
    RemoteEvent event = new RemoteEvent(SecurityConfigNotification.EVENT_TYPE, notif);
    repo.onAsyncEvent(event);
    
    verify(security, never()).addOrUpdateRole(eq("admin"), anySetOf(Permission.class));
    verify(appkeys, never()).addOrUpdateApplicationKey(eq("test-app"), eq("test-key"), eq("test-role"));
    verify(cluster, never()).dispatch(any(SecurityConfigNotification.class));
  }
  
  @Test
  public void testHandleSecurityConfigNotification_for_server_force() throws Exception {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));
    List<RoleConfig>   roles = Collects.arrayToList(new RoleConfig("admin", Collects.arrayToSet(Permission.values())));
    List<AppKeyConfig> keys  = Collects.arrayToList(new AppKeyConfig("test-app", "test-role", "test-key"));
    SecurityConfigNotification notif = new SecurityConfigNotification(roles, keys);
    notif.setForce(true);
    notif.addTarget(host.getEndpoint());
    
    RemoteEvent event = new RemoteEvent(SecurityConfigNotification.EVENT_TYPE, notif);
    repo.onAsyncEvent(event);
    
    verify(security).addOrUpdateRole(eq("admin"), anySetOf(Permission.class));
    verify(appkeys).addOrUpdateApplicationKey(eq("test-app"), eq("test-key"), eq("test-role"));
    verify(cluster).dispatch(any(SecurityConfigNotification.class));
  }
  
  @Test
  public void testHandleSecurityConfig_pull_disabled() throws Exception {
    repoConfig.setPullSecurityConfigEnabled(false);
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    List<RoleConfig>   roles = Collects.arrayToList(new RoleConfig("admin", Collects.arrayToSet(Permission.values())));
    List<AppKeyConfig> keys  = Collects.arrayToList(new AppKeyConfig("test-app", "test-role", "test-key"));
    SecurityConfigNotification notif = new SecurityConfigNotification(roles, keys);
    notif.addTarget(host.getEndpoint());
    
    RemoteEvent event = new RemoteEvent(SecurityConfigNotification.EVENT_TYPE, notif);
    repo.onAsyncEvent(event);
    
    verify(security, never()).addOrUpdateRole(eq("admin"), anySetOf(Permission.class));
    verify(appkeys, never()).addOrUpdateApplicationKey(eq("test-app"), eq("test-key"), eq("test-role"));
    verify(cluster).dispatch(any(SecurityConfigNotification.class));
  }
  
  @Test
  public void testSecurityConfigNotification_host_not_targeted() throws Exception {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
    List<RoleConfig>   roles = Collects.arrayToList(new RoleConfig("admin", Collects.arrayToSet(Permission.values())));
    List<AppKeyConfig> keys  = Collects.arrayToList(new AppKeyConfig("test-app", "test-role", "test-key"));
    SecurityConfigNotification notif = new SecurityConfigNotification(roles, keys);
    notif.addTarget(createCorusHost(RepoRole.CLIENT).getEndpoint());
    
    RemoteEvent event = new RemoteEvent(SecurityConfigNotification.EVENT_TYPE, notif);
    repo.onAsyncEvent(event);
    
    verify(security, never()).addOrUpdateRole(eq("admin"), anySetOf(Permission.class));
    verify(appkeys, never()).addOrUpdateApplicationKey(eq("test-app"), eq("test-key"), eq("test-role"));
    verify(cluster).dispatch(any(SecurityConfigNotification.class));
  }
  
  @Test
  public void testOnCorusHostAddEvent_client() {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
      
    repo.onCorusHostAddedEvent(new CorusHostAddedEvent(createCorusHost(RepoRole.SERVER)));
    
    verify(tasks).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class));
  }
  
  @Test
  public void testOnCorusHostAddEvent_client_has_distributions() {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
      
    when(deployer.getDistributions(any())).thenReturn(Arrays.asList(new Distribution("test", "1.0")));

    repo.onCorusHostAddedEvent(new CorusHostAddedEvent(createCorusHost(RepoRole.SERVER)));
    
    verify(tasks, never()).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class));
  }
  
  @Test
  public void testOnCorusHostAddEvent_new_node_is_client() {
    host.setRepoRole(RepoRole.CLIENT);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.CLIENT));
  
    repo.onCorusHostAddedEvent(new CorusHostAddedEvent(createCorusHost(RepoRole.CLIENT)));
    
    verify(tasks, never()).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class));
  }

  @Test
  public void testOnCorusHostAddEvent_server_sync() {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new RepoServerSyncStrategy(RepoRole.SERVER));
    
    repo.onCorusHostAddedEvent(new CorusHostAddedEvent(createCorusHost(RepoRole.SERVER)));
    
    verify(tasks).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class));
  }
  
  @Test
  public void testOnCorusHostAddEvent_server_sync_has_distributions() {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new RepoServerSyncStrategy(RepoRole.SERVER));

    when(deployer.getDistributions(any())).thenReturn(Arrays.asList(new Distribution("test", "1.0")));

    repo.onCorusHostAddedEvent(new CorusHostAddedEvent(createCorusHost(RepoRole.SERVER)));
    
    verify(tasks, never()).executeBackground(any(Task.class), any(Void.class), any(BackgroundTaskConfig.class));
  }
  
  @Test
  public void testOnCorusHostAddEvent_no_server_sync() {
    host.setRepoRole(RepoRole.SERVER);
    repo.setRepoStrategy(new DefaultRepoStrategy(RepoRole.SERVER));

    repo.onCorusHostAddedEvent(new CorusHostAddedEvent(createCorusHost(RepoRole.SERVER)));
    
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
  public void testChangeRole_no_server_sync() {
    host.setRepoRole(RepoRole.CLIENT);
    repoConfig.setRepoServerSyncEnabled(true);
    
    repo.changeRole(RepoRole.SERVER);
    
    assertTrue(repo.getStrategy() instanceof RepoServerSyncStrategy);
  }
  
  @Test
  public void testStartCheckNodeTask_with_check_state_disabled() {
    repoConfig.setCheckStateEnabled(false);
    
    repo.startCheckNodeTask();
    
    verify(tasks, never()).executeBackground(any(Task.class), any(), any());
  }
  
  @Test
  public void testStartCheckNodeTask_with_repo_client_and_explicit_config() {
    host.setRepoRole(RepoRole.CLIENT);
    repoConfig.setCheckStateAutomatic(false);
    repoConfig.setCheckStateMaxRandomHosts(3);
    repoConfig.setCheckStateRandomHostsEnabled(true);
    
    repo.startCheckNodeTask();
    
    ArgumentCaptor<CheckNodeStateTask> captor = ArgumentCaptor.forClass(CheckNodeStateTask.class);
    verify(tasks).executeBackground(captor.capture(), any(), any());
    
    CheckNodeStateTask submitted = captor.getValue();
    assertTrue(submitted.isCheckRandomHostsEnabled());
    assertEquals(3, submitted.getMaxRandomHosts());
  }
  
  @Test
  public void testStartCheckNodeTask_with_repo_client_and_explicit_random_hosts_disabled() {
    host.setRepoRole(RepoRole.CLIENT);
    repoConfig.setCheckStateAutomatic(false);
    repoConfig.setCheckStateMaxRandomHosts(3);
    repoConfig.setCheckStateRandomHostsEnabled(false);
    
    repo.startCheckNodeTask();
    
    ArgumentCaptor<CheckNodeStateTask> captor = ArgumentCaptor.forClass(CheckNodeStateTask.class);
    verify(tasks).executeBackground(captor.capture(), any(), any());
    
    CheckNodeStateTask submitted = captor.getValue();
    assertFalse(submitted.isCheckRandomHostsEnabled());
    assertEquals(3, submitted.getMaxRandomHosts());
  }
  
  @Test
  public void testStartCheckNodeTask_with_repo_server_and_explicit_config() {
    host.setRepoRole(RepoRole.SERVER);
    repoConfig.setCheckStateAutomatic(false);
    repoConfig.setCheckStateMaxRandomHosts(3);
    repoConfig.setCheckStateRandomHostsEnabled(true);
    
    repo.startCheckNodeTask();
    
    ArgumentCaptor<CheckNodeStateTask> captor = ArgumentCaptor.forClass(CheckNodeStateTask.class);
    verify(tasks).executeBackground(captor.capture(), any(), any());
    
    CheckNodeStateTask submitted = captor.getValue();
    assertTrue(submitted.isCheckRandomHostsEnabled());
    assertEquals(3, submitted.getMaxRandomHosts());
  }
  
  @Test
  public void testStartCheckNodeTask_with_repo_server_and_explicit_random_hosts_disabled() {
    host.setRepoRole(RepoRole.SERVER);
    repoConfig.setCheckStateAutomatic(false);
    repoConfig.setCheckStateMaxRandomHosts(3);
    repoConfig.setCheckStateRandomHostsEnabled(false);
    
    repo.startCheckNodeTask();
    
    ArgumentCaptor<CheckNodeStateTask> captor = ArgumentCaptor.forClass(CheckNodeStateTask.class);
    verify(tasks).executeBackground(captor.capture(), any(), any());
    
    CheckNodeStateTask submitted = captor.getValue();
    assertFalse(submitted.isCheckRandomHostsEnabled());
    assertEquals(3, submitted.getMaxRandomHosts());
  }
  
  @Test
  public void testStartCheckNodeTask_with_repo_client_and_automatic_config() {
    host.setRepoRole(RepoRole.CLIENT);
    repoConfig.setCheckStateAutomatic(true);
    repoConfig.setCheckStateMaxRandomHosts(3);
    // setting to false to insure automatic determination is used
    repoConfig.setCheckStateRandomHostsEnabled(false);
    
    repo.startCheckNodeTask();
    
    ArgumentCaptor<CheckNodeStateTask> captor = ArgumentCaptor.forClass(CheckNodeStateTask.class);
    verify(tasks).executeBackground(captor.capture(), any(), any());
    
    CheckNodeStateTask submitted = captor.getValue();
    assertTrue(submitted.isCheckRandomHostsEnabled());
    assertEquals(3, submitted.getMaxRandomHosts());
  }
  
  @Test
  public void testStartCheckNodeTask_with_repo_server_and_automatic_config() {
    host.setRepoRole(RepoRole.SERVER);
    repoConfig.setCheckStateAutomatic(true);
    repoConfig.setCheckStateMaxRandomHosts(3);
    // setting to true to insure automatic determination is used
    repoConfig.setCheckStateRandomHostsEnabled(true);
    
    repo.startCheckNodeTask();
    
    ArgumentCaptor<CheckNodeStateTask> captor = ArgumentCaptor.forClass(CheckNodeStateTask.class);
    verify(tasks).executeBackground(captor.capture(), any(), any());
    
    CheckNodeStateTask submitted = captor.getValue();
    assertFalse(submitted.isCheckRandomHostsEnabled());
    assertEquals(0, submitted.getMaxRandomHosts());
  }
  
  @Test
  public void testStartCheckNodeTask_with_repo_client_and_automatic_and_check_disabled() {
    host.setRepoRole(RepoRole.CLIENT);
    repoConfig.setCheckStateEnabled(false);
    repoConfig.setCheckStateAutomatic(true);
    repoConfig.setCheckStateMaxRandomHosts(3);
    
    repo.startCheckNodeTask();
    
    ArgumentCaptor<CheckNodeStateTask> captor = ArgumentCaptor.forClass(CheckNodeStateTask.class);
    verify(tasks, never()).executeBackground(captor.capture(), any(), any());
  }
  
  @Test
  public void testStartCheckNodeTask_with_repo_server_and_automatic_and_check_disabled() {
    host.setRepoRole(RepoRole.SERVER);
    repoConfig.setCheckStateEnabled(false);
    repoConfig.setCheckStateAutomatic(true);
    repoConfig.setCheckStateMaxRandomHosts(3);
    
    repo.startCheckNodeTask();
    
    ArgumentCaptor<CheckNodeStateTask> captor = ArgumentCaptor.forClass(CheckNodeStateTask.class);
    verify(tasks, never()).executeBackground(captor.capture(), any(), any());
  }
}
