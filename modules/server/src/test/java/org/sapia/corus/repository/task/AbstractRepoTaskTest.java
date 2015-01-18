package org.sapia.corus.repository.task;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.sapia.corus.client.services.cluster.ClusterManager;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.client.services.security.ApplicationKeyManager;
import org.sapia.corus.client.services.security.SecurityModule;
import org.sapia.corus.core.InternalServiceContext;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.repository.RepositoryConfigurationImpl;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskManager;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.rmi.server.transport.socket.TcpSocketAddress;

public abstract class AbstractRepoTaskTest {

  protected TaskExecutionContext   taskContext;
  protected ServerContext          serverContext;
  protected InternalServiceContext serviceContext;
  protected ClusterManager         cluster;
  protected EventChannel           eventChannel;
  protected Configurator           configurator;
  protected TaskManager            taskMan;
  protected Processor              processor;
  protected CorusHost              node;
  protected SecurityModule         security;
  protected ApplicationKeyManager  appkeys;
  protected RepositoryConfigurationImpl repoConfig;
  
  protected void doSetUp() {
    repoConfig     = new RepositoryConfigurationImpl();
    taskContext    = mock(TaskExecutionContext.class);
    serverContext  = mock(ServerContext.class);
    serviceContext = mock(InternalServiceContext.class);
    cluster        = mock(ClusterManager.class);
    configurator   = mock(Configurator.class);
    taskMan        = mock(TaskManager.class);
    eventChannel   = mock(EventChannel.class);
    processor      = mock(Processor.class);
    security       = mock(SecurityModule.class);
    appkeys        = mock(ApplicationKeyManager.class);
    
    node = CorusHost.newInstance(new Endpoint(new TcpSocketAddress("test", 1001), new TcpSocketAddress("test", 1001)), "test", "test");
    node.setRepoRole(RepoRole.CLIENT);
    
    when(taskContext.getServerContext()).thenReturn(serverContext);
    when(serverContext.getServices()).thenReturn(serviceContext);
    when(serverContext.getCorusHost()).thenReturn(node);
    when(serviceContext.getClusterManager()).thenReturn(cluster);
    when(serviceContext.getConfigurator()).thenReturn(configurator);
    when(serviceContext.getProcessor()).thenReturn(processor);
    when(serviceContext.getSecurityModule()).thenReturn(security);
    when(serviceContext.getAppKeyManager()).thenReturn(appkeys);
    when(taskContext.getTaskManager()).thenReturn(taskMan);
    when(cluster.getEventChannel()).thenReturn(eventChannel);
  }

}
