package org.sapia.corus.client.rest.resources;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.rest.RestRequest;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.corus.client.facade.PortManagementFacade;
import org.sapia.corus.client.rest.ConnectorPool;
import org.sapia.corus.client.rest.PartitionService;
import org.sapia.corus.client.rest.RequestContext;
import org.sapia.corus.client.rest.async.AsynchronousCompletionService;
import org.sapia.corus.client.rest.resources.PortResource;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.port.PortRange;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;

@RunWith(MockitoJUnitRunner.class)
public class PortResourceTest {
  
  @Mock
  private CorusConnector connector;
  
  @Mock
  private ConnectorPool connectors;
  
  @Mock
  private CorusConnectionContext connection;
  
  @Mock
  private AsynchronousCompletionService async;
  
  @Mock
  private PartitionService partitions;
  
  @Mock
  private RestRequest request;
  
  @Mock
  private PortManagementFacade ports;
  
  private PortResource resource;
  private Results<List<PortRange>> results;
  
  @Before
  public void setUp() throws Exception {
    resource = new PortResource();
    
    results = new Results<List<PortRange>>();
    int rangeCount = 0;
    for (int i = 0; i < 5; i++) {
      CorusHost host = CorusHost.newInstance(
          new Endpoint(new TCPAddress("test", "host-" + i, i), mock(ServerAddress.class)), 
          "os-" + i, 
          "jvm-" + i,
          mock(PublicKey.class)
      );
      host.setHostName("hostname-" + i);
      host.setRepoRole(RepoRole.CLIENT);
      List<PortRange> ranges = new ArrayList<PortRange>();
      for (int j = 0; j < 5; j++) {
        PortRange pr = new PortRange(String.format("range-%s", rangeCount), rangeCount + 1, rangeCount + 2);
        pr.acquire();
        ranges.add(pr);
        rangeCount++;
      }
      Result<List<PortRange>> result = new Result<List<PortRange>>(host, ranges, Result.Type.COLLECTION);
      results.addResult(result);
    }
    
    when(connectors.acquire()).thenReturn(connector);
    when(connection.getDomain()).thenReturn("test-cluster");
    when(connection.getVersion()).thenReturn("test-version");
    when(connector.getPortManagementFacade()).thenReturn(ports);
    when(ports.getPortRanges(any(ClusterInfo.class)))
      .thenReturn(results);
    when(connector.getContext()).thenReturn(connection);
    when(request.getValue("corus:host")).thenReturn(new Value("corus:host", "localhost:33000"));
    when(request.getValue("p")).thenReturn(new Value("p", "test-profile"));
    when(request.getValue(anyString(), anyString())).thenReturn(new Value("test", "*"));
  }
  
  @Test
  public void testGetPortRangesForClusters() {
    String response = resource.getPortRangesForCluster(new RequestContext(request, connector, async, partitions, connectors));
    JSONArray json = JSONArray.fromObject(response);
    int count = 0;
    for (int i = 0; i < json.size(); i++) {
      JSONObject r = json.getJSONObject(i).getJSONObject("data");
      doCheckPortRange(r, count++);
    }
  }

  @Test
  public void testgetPortRangesForClusterAndHost() {
    String response = resource.getPortRangesForHost(new RequestContext(request, connector, async, partitions, connectors));
    JSONArray json = JSONArray.fromObject(response);
    int count = 0;
    for (int i = 0; i < json.size(); i++) {
      JSONObject r = json.getJSONObject(i).getJSONObject("data");
      doCheckPortRange(r, count++);
    }
  }

  
  private void doCheckPortRange(JSONObject range, int i) {
    assertEquals("range-" + i, range.getString("name"));
    assertEquals(i + 1, range.getInt("min"));
    assertEquals(i + 2, range.getInt("max"));
  }  

}
