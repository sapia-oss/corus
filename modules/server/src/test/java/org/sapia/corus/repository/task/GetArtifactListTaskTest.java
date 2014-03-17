package org.sapia.corus.repository.task;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.repository.ArtifactListRequest;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.transport.socket.TcpSocketAddress;
import org.sapia.ubik.util.Collects;

public class GetArtifactListTaskTest extends AbstractRepoTaskTest {

  private Set<CorusHost>       emptyHosts, clientHosts, serverHosts;
  private GetArtifactListTask task;
  
  @Before
  public void setUp() {
    super.doSetUp();
    
    emptyHosts     = new HashSet<CorusHost>();
    
    CorusHost client = CorusHost.newInstance(new Endpoint(new TcpSocketAddress("test", 1000), new TcpSocketAddress("test", 1000)), "test", "test");
    client.setRepoRole(RepoRole.CLIENT);
    clientHosts    = Collects.arrayToSet(client);
    
    CorusHost server = CorusHost.newInstance(new Endpoint(new TcpSocketAddress("test", 1000), new TcpSocketAddress("test", 1000)), "test", "test");
    server.setRepoRole(RepoRole.SERVER);
    serverHosts    = Collects.arrayToSet(server);
    
    task           = new GetArtifactListTask();
   }
  
  @Test
  public void testRunWithEmptyHosts() throws Throwable {
    when(cluster.getHosts()).thenReturn(emptyHosts);
    task.execute(taskContext, null);
    verify(eventChannel, never()).dispatch(any(ServerAddress.class), anyString(), any(Object.class));
  }

  @Test
  public void testRunWithClientHosts() throws Throwable {
    when(cluster.getHosts()).thenReturn(clientHosts);
    task.execute(taskContext, null);
    verify(eventChannel, never()).dispatch(any(ServerAddress.class), anyString(), any(Object.class));
  }
  
  @Test
  public void testRunWithServerHosts() throws Throwable {
    when(cluster.getHosts()).thenReturn(serverHosts);
    task.execute(taskContext, null);
    verify(eventChannel, times(1)).dispatch(eq(new TcpSocketAddress("test", 1000)), eq(ArtifactListRequest.EVENT_TYPE), any(ArtifactListRequest.class));
  }
}
