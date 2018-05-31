package org.sapia.corus.repository.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.Mock;
import org.sapia.corus.client.common.reference.Reference;
import org.sapia.corus.client.services.ModuleState;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.repository.ArtifactListRequest;
import org.sapia.corus.repository.PullProcessState;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.transport.socket.TcpSocketAddress;
import org.sapia.ubik.util.Collects;

@RunWith(MockitoJUnitRunner.class)
public class CheckNodeStateTaskTest extends AbstractRepoTaskTest {

  @Mock
  private Reference<ModuleState> moduleState;
  private Set<CorusHost>         emptyHosts, clientHosts, serverHosts;
  private PullProcessState       pullProcessState; 
  private CheckNodeStateTask     task;
  
  @Before
  public void setUp() {
    super.doSetUp();
    
    emptyHosts     = new HashSet<CorusHost>();
    
    CorusHost client = CorusHost.newInstance("test-node", new Endpoint(new TcpSocketAddress("test", 2000), new TcpSocketAddress("test", 2000)), "test", "test", mock(PublicKey.class));
    client.setRepoRole(RepoRole.CLIENT);
    clientHosts    = Collects.arrayToSet(client);
    
    CorusHost server = CorusHost.newInstance("test-node", new Endpoint(new TcpSocketAddress("test", 1000), new TcpSocketAddress("test", 1000)), "test", "test", mock(PublicKey.class));
    server.setRepoRole(RepoRole.SERVER);
    serverHosts    = Collects.arrayToSet(server);
    
    pullProcessState = new PullProcessState();
    task             = new CheckNodeStateTask(moduleState, pullProcessState, 5, true);
    
    when(moduleState.setIf(ModuleState.BUSY, ModuleState.IDLE)).thenReturn(true);
   }
  
  @Test
  public void testRun_with_empty_hosts() throws Throwable {
    when(cluster.getHosts()).thenReturn(emptyHosts);
    
    task.execute(taskContext, null);
    
    verify(eventChannel, never()).dispatch(any(ServerAddress.class), anyString(), any(Object.class));
    assertThat(pullProcessState.getContactedRepoServer()).isEmpty();
  }
  
  @Test
  public void testRun_with_module_state_busy() throws Throwable {
    when(moduleState.setIf(ModuleState.BUSY, ModuleState.IDLE)).thenReturn(false);
    when(cluster.getHosts()).thenReturn(serverHosts);
    
    task.execute(taskContext, null);
    
    verify(eventChannel, never()).dispatch(eq(new TcpSocketAddress("test", 2000)), eq(ArtifactListRequest.EVENT_TYPE), any());
  }
  
  @Test
  public void testRun_with_module_state_idle() throws Throwable {
    when(cluster.getHosts()).thenReturn(serverHosts);
    
    task.execute(taskContext, null);
    
    verify(eventChannel, never()).dispatch(eq(new TcpSocketAddress("test", 2000)), eq(ArtifactListRequest.EVENT_TYPE), any());
    verify(moduleState).setIf(ModuleState.BUSY, ModuleState.IDLE);
  }

  @Test
  public void testRun_with_client_hosts() throws Throwable {
    when(cluster.getHosts()).thenReturn(clientHosts);
    
    task.execute(taskContext, null);
    
    ArgumentCaptor<ArtifactListRequest> captor = ArgumentCaptor.forClass(ArtifactListRequest.class);
    verify(eventChannel, times(1)).dispatch(eq(new TcpSocketAddress("test", 2000)), eq(ArtifactListRequest.EVENT_TYPE), captor.capture());
    assertThat(captor.getValue().isForce()).isTrue();
    assertThat(pullProcessState.getContactedRepoServer()).containsAll(clientHosts);
  }
  
  @Test
  public void testRun_with_client_hosts_and_random_hosts_disabled() throws Throwable {
    task = new CheckNodeStateTask(moduleState, pullProcessState, 5, false);
    when(cluster.getHosts()).thenReturn(clientHosts);
    
    task.execute(taskContext, null);
    
    ArgumentCaptor<ArtifactListRequest> captor = ArgumentCaptor.forClass(ArtifactListRequest.class);
    verify(eventChannel, never()).dispatch(eq(new TcpSocketAddress("test", 2000)), eq(ArtifactListRequest.EVENT_TYPE), captor.capture());
    assertThat(pullProcessState.getContactedRepoServer()).isEmpty();
  }
  
  @Test
  public void testRun_with_server_hosts() throws Throwable {
    when(cluster.getHosts()).thenReturn(serverHosts);
    
    task.execute(taskContext, null);
    
    ArgumentCaptor<ArtifactListRequest> captor = ArgumentCaptor.forClass(ArtifactListRequest.class);
    verify(eventChannel, times(1)).dispatch(eq(new TcpSocketAddress("test", 1000)), eq(ArtifactListRequest.EVENT_TYPE), captor.capture());
    assertThat(captor.getValue().isForce()).isTrue();
    assertThat(pullProcessState.getContactedRepoServer()).containsAll(serverHosts);
  }

}
