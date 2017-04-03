package org.sapia.corus.repository.task;


import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.repository.ArtifactListRequest;
import org.sapia.corus.repository.PullProcessState;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.transport.socket.TcpSocketAddress;
import org.sapia.ubik.util.Collects;

public class GetArtifactListTaskTest extends AbstractRepoTaskTest {

  private Set<CorusHost>      emptyHosts, clientHosts, serverHosts;
  private PullProcessState    pullProcessState; 
  private GetArtifactListTask task;
  
  @Before
  public void setUp() {
    super.doSetUp();
    
    emptyHosts     = new HashSet<CorusHost>();
    
    CorusHost client = CorusHost.newInstance("test-node", new Endpoint(new TcpSocketAddress("test", 1000), new TcpSocketAddress("test", 1000)), "test", "test", mock(PublicKey.class));
    client.setRepoRole(RepoRole.CLIENT);
    clientHosts    = Collects.arrayToSet(client);
    
    CorusHost server = CorusHost.newInstance("test-node", new Endpoint(new TcpSocketAddress("test", 1000), new TcpSocketAddress("test", 1000)), "test", "test", mock(PublicKey.class));
    server.setRepoRole(RepoRole.SERVER);
    serverHosts    = Collects.arrayToSet(server);
    
    pullProcessState = new PullProcessState();
    task           = new GetArtifactListTask(pullProcessState);
   }
  
  @Test
  public void testRunWithEmptyHosts() throws Throwable {
    when(cluster.getHosts()).thenReturn(emptyHosts);
    
    task.execute(taskContext, null);
    
    verify(eventChannel, never()).dispatch(any(ServerAddress.class), anyString(), any(Object.class));
    assertThat(pullProcessState.getContactedRepoServer()).isEmpty();
  }

  @Test
  public void testRunWithClientHosts() throws Throwable {
    when(cluster.getHosts()).thenReturn(clientHosts);
    
    task.execute(taskContext, null);
    
    verify(eventChannel, never()).dispatch(any(ServerAddress.class), anyString(), any(Object.class));
    assertThat(pullProcessState.getContactedRepoServer()).isEmpty();
  }
  
  @Test
  public void testRunWithServerHosts() throws Throwable {
    when(cluster.getHosts()).thenReturn(serverHosts);
    
    task.execute(taskContext, null);
    
    verify(eventChannel, times(1)).dispatch(eq(new TcpSocketAddress("test", 1000)), eq(ArtifactListRequest.EVENT_TYPE), any(ArtifactListRequest.class));
    assertThat(pullProcessState.getContactedRepoServer()).containsAll(serverHosts);
  }
  
}
