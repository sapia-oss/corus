package org.sapia.corus.client.rest.resources;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.common.rest.RestRequest;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.corus.client.rest.ConnectorPool;
import org.sapia.corus.client.rest.PartitionService;
import org.sapia.corus.client.rest.RequestContext;
import org.sapia.corus.client.rest.async.AsynchronousCompletionService;
import org.sapia.corus.client.rest.resources.ClusterResource;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;

@RunWith(MockitoJUnitRunner.class)
public class ClusterResourceTest {

  @Mock
  private CorusConnector connector;
  
  @Mock
  private ConnectorPool connectors;
  
  @Mock
  private CorusConnectionContext connection;
  
  @Mock
  private AsynchronousCompletionService  async;
  
  @Mock
  private PartitionService partitions;
  
  @Mock
  private RestRequest request;
  
  private List<CorusHost> hosts;
  private RequestContext context;
  private ClusterResource   resource;
  
  @Before
  public void setUp() {
    context  = new RequestContext(request, connector, async, partitions, connectors);
    resource = new ClusterResource();
    
    when(connector.getContext()).thenReturn(connection);
    when(connectors.acquire()).thenReturn(connector);
    
    hosts = new ArrayList<CorusHost>();
    for (int i = 0; i < 5; i++) {
      CorusHost host = CorusHost.newInstance(
          "test-node", 
          new Endpoint(new TCPAddress("test", "host-" + i, i), mock(ServerAddress.class)), 
          "os-" + i, 
          "jvm-" + i,
          mock(PublicKey.class)
      );
      host.setHostName("hostname-" + i);
      host.setRepoRole(RepoRole.CLIENT);
      hosts.add(host);
    }

    when(connection.getDomain()).thenReturn("test-cluster");
    when(connection.getVersion()).thenReturn("test-version");
    when(connection.getServerHost()).thenReturn(hosts.remove(0));
    when(connection.getOtherHosts()).thenReturn(hosts);
  }

  @Test
  public void testGetHostsForCluster() {
    String response = resource.getHostsForCluster(context);
    JSONArray json = JSONArray.fromObject(response);
    for (int i = 0; i < json.size(); i++) {
      doAssertHost(json.getJSONObject(i), i);
    }
  }
  
  private void doAssertHost(JSONObject host, int i) {
    assertEquals("test-cluster", host.getString("cluster"));
    assertEquals("test-version", host.getString("corusVersion"));
    assertEquals("hostname-" + i, host.getString("hostName"));
    assertEquals("host-" + i, host.getString("hostAddress"));
    assertEquals(i, host.getInt("port"));
    assertEquals("os-" + i, host.getString("osInfo"));
    assertEquals("jvm-" + i, host.getString("jvmInfo"));
    assertEquals(RepoRole.CLIENT.name(), host.getString("repoRole"));
  }
}
