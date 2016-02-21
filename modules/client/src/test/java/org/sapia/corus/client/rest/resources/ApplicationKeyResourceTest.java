package org.sapia.corus.client.rest.resources;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.rest.RestRequest;
import org.sapia.corus.client.common.rest.Value;
import org.sapia.corus.client.facade.ApplicationKeyManagementFacade;
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.corus.client.rest.ConnectorPool;
import org.sapia.corus.client.rest.PartitionService;
import org.sapia.corus.client.rest.RequestContext;
import org.sapia.corus.client.rest.async.AsynchronousCompletionService;
import org.sapia.corus.client.rest.resources.ApplicationKeyResource;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.security.ApplicationKeyManager.AppKeyConfig;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationKeyResourceTest {

  @Mock 
  private CorusConnector                 connector;
  
  @Mock
  private ConnectorPool                  connectors;
  
  @Mock
  private CorusConnectionContext         connection;
  
  @Mock
  private AsynchronousCompletionService  async;
  
  @Mock
  private PartitionService               partitions;
  
  @Mock
  private RestRequest                    request;
  
  @Mock
  private ApplicationKeyManagementFacade facade;
  
  private Results<List<AppKeyConfig>>    results;

  private RequestContext                 context;
 
  private List<CorusHost>                hosts;

  private ApplicationKeyResource         resource;
  
  @Before
  public void setUp() {
    context  = new RequestContext(request, connector, async, partitions, connectors);
    resource = new ApplicationKeyResource();
    
    when(connectors.acquire()).thenReturn(connector);
    when(connector.getContext()).thenReturn(connection);
    
    results = new Results<List<AppKeyConfig>>();
    int count = 0;
    for (int i = 0; i < 5; i++) {
      CorusHost host = CorusHost.newInstance(
          new Endpoint(new TCPAddress("test", "host-" + i, i), mock(ServerAddress.class)), 
          "os-" + i, 
          "jvm-" + i,
          mock(PublicKey.class)
      );
      host.setHostName("hostname-" + i);
      host.setRepoRole(RepoRole.CLIENT);
      List<AppKeyConfig> appkeys = new ArrayList<AppKeyConfig>();
      for (int j = 0; j < 5; j++) {
        AppKeyConfig appkey = new AppKeyConfig("appId-" + count, "role-" + count, "key-" + count);
        appkeys.add(appkey);
        count++;
      }
      Result<List<AppKeyConfig>> result = new Result<List<AppKeyConfig>>(host, appkeys, Result.Type.COLLECTION);
      results.addResult(result);
    }

    when(connection.getDomain()).thenReturn("test-cluster");
    when(connection.getVersion()).thenReturn("test-version");
    when(connection.getOtherHosts()).thenReturn(hosts);
    when(request.getValue("corus:host")).thenReturn(new Value("corus:host", "localhost:33000"));
    when(request.getValue("a", "*")).thenReturn(new Value("a", "*"));
    when(connector.getApplicationKeyManagementFacade()).thenReturn(facade);
    when(facade.getAppKeyInfos(any(ArgMatcher.class), any(ClusterInfo.class)))
      .thenReturn(results);
  }
  
  @Test
  public void testGetAppKeysForCluster() {
    String response = resource.getAppKeysForCluster(context);
    JSONArray json = JSONArray.fromObject(response);
    int count = 0;
    for (int i = 0; i < json.size(); i++) {
      JSONObject appkey = json.getJSONObject(i).getJSONObject("data");
      doCheckApplicationKey(appkey, count++);
    }
  }

  @Test
  public void testGetAppKeysForHost() {
    String response = resource.getAppKeysForHost(context);
    JSONArray json = JSONArray.fromObject(response);
    int count = 0;
    for (int i = 0; i < json.size(); i++) {
      JSONObject appkey = json.getJSONObject(i).getJSONObject("data");
      doCheckApplicationKey(appkey, count++);
    }
  }
  
  @Test
  public void testCreateAppKeyForCluster() {
    when(request.getValue("corus:appId")).thenReturn(new Value("corus:appId", "1234"));
    when(request.getValue(eq("k"), anyString())).thenReturn(new Value("k", "abcdef"));
    when(request.getValue("r")).thenReturn(new Value("r", "admin"));
    
    resource.createAppKeyForCluster(context);
    
    verify(facade).createApplicationKey(eq("1234"), eq("abcdef"), eq("admin"), any(ClusterInfo.class));
  }

  @Test
  public void testCreateAppKeyForHost() {
    when(request.getValue("corus:appId")).thenReturn(new Value("corus:appId", "1234"));
    when(request.getValue(eq("k"), anyString())).thenReturn(new Value("k", "abcdef"));
    when(request.getValue("r")).thenReturn(new Value("r", "admin"));

    resource.createAppKeyForHost(context);
    
    verify(facade).createApplicationKey(eq("1234"), eq("abcdef"), eq("admin"), any(ClusterInfo.class));
  }

  @Test
  public void testUpdateAppKeyForCluster() {
    when(request.getValue("corus:appId")).thenReturn(new Value("corus:appId", "1234"));
    when(request.getValue(eq("corus:key"), anyString())).thenReturn(new Value("corus:key", "abcdef"));

    resource.updateAppKeyForCluster(context);
    
    verify(facade).changeApplicationKey(eq("1234"), eq("abcdef"), any(ClusterInfo.class));
  }

  @Test
  public void testUpdateAppKeyForHost() {
    when(request.getValue("corus:appId")).thenReturn(new Value("corus:appId", "1234"));
    when(request.getValue(eq("corus:key"), anyString())).thenReturn(new Value("corus:key", "abcdef"));

    resource.updateAppKeyForHost(context);
    
    verify(facade).changeApplicationKey(eq("1234"), eq("abcdef"), any(ClusterInfo.class));
  }

  @Test
  public void testUpdateAppKeyRoleForCluster() {
    when(request.getValue("corus:appId")).thenReturn(new Value("corus:appId", "1234"));
    when(request.getValue("corus:role")).thenReturn(new Value("corus:role", "admin"));

    resource.updateAppKeyRoleForCluster(context);
    
    verify(facade).changeRole(eq("1234"), eq("admin"), any(ClusterInfo.class));
  }

  @Test
  public void testUpdateAppKeyRoleForHost() {
    when(request.getValue("corus:appId")).thenReturn(new Value("corus:appId", "1234"));
    when(request.getValue("corus:role")).thenReturn(new Value("corus:role", "admin"));

    resource.updateAppKeyRoleForHost(context);
    
    verify(facade).changeRole(eq("1234"), eq("admin"), any(ClusterInfo.class));
  }

  @Test
  public void testDeleteAppKeyForCluster() {
    when(request.getValue("corus:appId")).thenReturn(new Value("corus:appId", "1234"));

    resource.deleteAppKeyForCluster(context);
    
    verify(facade).removeAppKey(any(ArgMatcher.class), any(ClusterInfo.class));
  }

  @Test
  public void testDeleteRoleForHost() {
    when(request.getValue("corus:appId")).thenReturn(new Value("corus:appId", "1234"));

    resource.deleteAppKeyForHost(context);
    
    verify(facade).removeAppKey(any(ArgMatcher.class), any(ClusterInfo.class));
  }

  private void doCheckApplicationKey(JSONObject dist, int i) {
    assertEquals("appId-" + i, dist.getString("appId"));
    assertEquals("role-" + i, dist.getString("role"));
    assertEquals("key-" + i, dist.getString("key"));
  }

}
