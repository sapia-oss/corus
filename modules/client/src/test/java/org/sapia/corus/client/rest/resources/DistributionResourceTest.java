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
import org.sapia.corus.client.facade.DeployerFacade;
import org.sapia.corus.client.rest.ConnectorPool;
import org.sapia.corus.client.rest.PartitionService;
import org.sapia.corus.client.rest.RequestContext;
import org.sapia.corus.client.rest.async.AsynchronousCompletionService;
import org.sapia.corus.client.rest.resources.DistributionResource;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.Generic;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;

@RunWith(MockitoJUnitRunner.class)
public class DistributionResourceTest {
  
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
  private RestRequest    request;
  
  @Mock
  private DeployerFacade deployer;
  
  private DistributionResource        resource;
  private Results<List<Distribution>> results;
  private RequestContext              context;
  
  @Before
  public void setUp() {
    resource = new DistributionResource();
    
    results = new Results<List<Distribution>>();
    int distCount = 0;
    for (int i = 0; i < 5; i++) {
      CorusHost host = CorusHost.newInstance(
          new Endpoint(new TCPAddress("test", "host-" + i, i), mock(ServerAddress.class)), 
          "os-" + i, 
          "jvm-" + i,
          mock(PublicKey.class)
      );
      host.setHostName("hostname-" + i);
      host.setRepoRole(RepoRole.CLIENT);
      List<Distribution> distributions = new ArrayList<Distribution>();
      for (int j = 0; j < 5; j++) {
        Distribution dist = new Distribution(String.format("dist-%s", distCount), "1.0");
        ProcessConfig pc = new ProcessConfig();
        pc.setName("test-proc");
        
        Generic starter = new Generic();
        starter.setProfile("test-profile");
        pc.addStarter(starter);
        dist.addProcess(pc);
        distributions.add(dist);
        distCount++;
      }
      Result<List<Distribution>> result = new Result<List<Distribution>>(host, distributions, Result.Type.COLLECTION);
      results.addResult(result);
    }
    
    context = new RequestContext(request, connector, async, partitions, connectors);
    
    when(connectors.acquire()).thenReturn(connector);
    when(connection.getDomain()).thenReturn("test-cluster");
    when(connection.getVersion()).thenReturn("test-version");
    when(connector.getDeployerFacade()).thenReturn(deployer);
    when(deployer.getDistributions(any(DistributionCriteria.class), any(ClusterInfo.class)))
      .thenReturn(results);
    when(connector.getContext()).thenReturn(connection);
    when(request.getValue("corus:host")).thenReturn(new Value("corus:host", "localhost:33000"));
    when(request.getValue(anyString(), anyString())).thenReturn(new Value("test", "*"));
  }

  @Test
  public void testGetDistributionsForCluster() {
    String response = resource.getDistributionsForCluster(context);
    JSONArray json = JSONArray.fromObject(response);
    int count = 0;
    for (int i = 0; i < json.size(); i++) {
      JSONObject dist = json.getJSONObject(i).getJSONObject("data");
      doCheckDistribution(dist, count++);
    }
  }

  @Test
  public void testGetDistributionsForHost() {
    String response = resource.getDistributionsForHost(context);
    JSONArray json = JSONArray.fromObject(response);
    int count = 0;
    for (int i = 0; i < json.size(); i++) {
      JSONObject dist = json.getJSONObject(i).getJSONObject("data");
      doCheckDistribution(dist, count++);
    }
  }
  
  private void doCheckDistribution(JSONObject dist, int i) {
    assertEquals("1.0", dist.getString("version"));
    assertEquals("dist-" + i, dist.getString("name"));
    for (int j = 0; j < 5; j++) {
      JSONObject processConfig = dist.getJSONArray("processConfigs").getJSONObject(0);
      assertEquals("test-proc", processConfig.getString("name"));
      assertEquals("test-profile", processConfig.getJSONArray("profiles").getString(0));
    }
  }

}
