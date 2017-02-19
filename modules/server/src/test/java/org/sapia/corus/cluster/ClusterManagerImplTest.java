package org.sapia.corus.cluster;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.security.PublicKey;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.cluster.event.CorusHostAddedEvent;
import org.sapia.corus.client.services.cluster.event.CorusHostRemovedEvent;
import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.corus.client.services.http.HttpModule;
import org.sapia.corus.core.ServerContext;
import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.RemoteEvent;
import org.sapia.ubik.rmi.server.transport.socket.TcpSocketAddress;

@RunWith(MockitoJUnitRunner.class)
public class ClusterManagerImplTest {
  
  @Mock
  private HttpModule httpModule;
  
  @Mock
  private EventDispatcher eventDispatcher;
  
  @Mock
  private ServerContext serverContext;
  
  @Mock
  private EventChannel channel;
  
  private ClusterManagerImpl cluster;
  
  private int corusPort;

  @Before
  public void setUp() throws Exception {
    cluster = new ClusterManagerImpl();
    cluster.setServerContext(serverContext);
    cluster.setDispatcher(eventDispatcher);
    cluster.setHttpModule(httpModule);

    when(serverContext.getEventChannel()).thenReturn(channel);

    cluster.init();
    
  }

  @Test
  public void testOnCorusPubEvent() throws IOException {
    cluster.onAsyncEvent(new RemoteEvent(CorusPubEvent.class.getName(), new CorusPubEvent(createCorusHost())));
    
    verify(eventDispatcher).dispatch(isA(CorusHostAddedEvent.class));
  }
  
  @Test
  public void testOnCorusDiscoEvent() throws IOException {
    cluster.onAsyncEvent(new RemoteEvent(CorusDiscoEvent.class.getName(), new CorusDiscoEvent(createCorusHost())));
    
    verify(eventDispatcher).dispatch(isA(CorusHostAddedEvent.class));
  }
  
  @Test
  public void testAddNode() {
    cluster.addNode(createCorusHost());
    
    verify(eventDispatcher).dispatch(isA(CorusHostAddedEvent.class));  
  }
  
  @Test
  public void testRemoveNode() {
    CorusHost node = createCorusHost();
    cluster.addNode(node);
    cluster.removeNode(node.getNode());
    
    verify(eventDispatcher).dispatch(isA(CorusHostRemovedEvent.class));  
  }
  
  private CorusHost createCorusHost() {
    CorusHost host = CorusHost.newInstance("test-node", new Endpoint(new TcpSocketAddress("test", corusPort++), new TcpSocketAddress("test", corusPort++)), "testOsInfo", "testVMInfo", mock(PublicKey.class));
    host.setRepoRole(RepoRole.SERVER);
    return host;
  }

}
