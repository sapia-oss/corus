package org.sapia.corus.client.rest.resources;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
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
import org.sapia.corus.client.facade.CorusConnectionContext;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.corus.client.facade.SecurityManagementFacade;
import org.sapia.corus.client.rest.ConnectorPool;
import org.sapia.corus.client.rest.PartitionService;
import org.sapia.corus.client.rest.RequestContext;
import org.sapia.corus.client.rest.async.AsynchronousCompletionService;
import org.sapia.corus.client.rest.resources.RoleResource;
import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.corus.client.services.cluster.CorusHost.RepoRole;
import org.sapia.corus.client.services.cluster.Endpoint;
import org.sapia.corus.client.services.security.Permission;
import org.sapia.corus.client.services.security.SecurityModule.RoleConfig;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.util.Collects;

@RunWith(MockitoJUnitRunner.class)
public class RoleResourceTest {

  @Mock 
  private CorusConnector            connector;
  
  @Mock
  private ConnectorPool             connectors;
  
  @Mock
  private CorusConnectionContext    connection;
  
  @Mock
  private AsynchronousCompletionService async;
  
  @Mock
  private PartitionService partitions;
  
  @Mock
  private RestRequest               request;
  
  @Mock
  private SecurityManagementFacade  facade;
  
  private Results<List<RoleConfig>> results;

  private RequestContext            context;
 
  private List<CorusHost>           hosts;

  private RoleResource              resource;
  
  @Before
  public void setUp() {
    context  = new RequestContext(request, connector, async, partitions, connectors);
    resource = new RoleResource();
    
    when(connectors.acquire()).thenReturn(connector);
    when(connector.getContext()).thenReturn(connection);
    
    results = new Results<List<RoleConfig>>();
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
      List<RoleConfig> appkeys = new ArrayList<RoleConfig>();
      for (int j = 0; j < 5; j++) {
        RoleConfig appkey = new RoleConfig("role-" + count, Collects.arrayToSet(Permission.values()));
        appkeys.add(appkey);
        count++;
      }
      Result<List<RoleConfig>> result = new Result<List<RoleConfig>>(host, appkeys, Result.Type.COLLECTION);
      results.addResult(result);
    }

    when(connection.getDomain()).thenReturn("test-cluster");
    when(connection.getVersion()).thenReturn("test-version");
    when(connection.getOtherHosts()).thenReturn(hosts);
    when(request.getValue("corus:host")).thenReturn(new Value("corus:host", "localhost:33000"));
    when(request.getValue("n", "*")).thenReturn(new Value("n", "*"));
    when(connector.getSecurityManagementFacade()).thenReturn(facade);
    when(facade.getRoleConfig(any(ArgMatcher.class), any(ClusterInfo.class)))
      .thenReturn(results);
  }
  
  @Test
  public void testGetRolesForCluster() {
    String response = resource.getRolesForCluster(context);
    JSONArray json = JSONArray.fromObject(response);
    int count = 0;
    for (int i = 0; i < json.size(); i++) {
      JSONObject role = json.getJSONObject(i).getJSONObject("data");
      doCheckRole(role, count++);
    }
  }

  @Test
  public void testGetRolesForHost() {
    String response = resource.getRolesForCluster(context);
    JSONArray json = JSONArray.fromObject(response);
    int count = 0;
    for (int i = 0; i < json.size(); i++) {
      JSONObject role = json.getJSONObject(i).getJSONObject("data");
      doCheckRole(role, count++);
    }
  }
  
  @Test
  public void testCreateOrUpdateRoleForCluster() {
    when(request.getValue("permissions")).thenReturn(new Value("permissions", "rwxda"));
    when(request.getValue("corus:role")).thenReturn(new Value("corus:role", "admin"));
    
    resource.createOrUpdateRoleForCluster(context);
    
    verify(facade).addOrUpdateRole(eq("admin"), eq(Collects.arrayToSet(Permission.values())),any(ClusterInfo.class));
  }

  @Test
  public void testCreateOrUpdateRoleForHost() {
    when(request.getValue("permissions")).thenReturn(new Value("permissions", "rwxda"));
    when(request.getValue("corus:role")).thenReturn(new Value("corus:role", "admin"));
    
    resource.createOrUpdateRoleForCluster(context);
    
    verify(facade).addOrUpdateRole(eq("admin"), eq(Collects.arrayToSet(Permission.values())),any(ClusterInfo.class));
  }

  @Test
  public void testDeleteRoleCluster() {
    when(request.getValue("corus:role")).thenReturn(new Value("corus:role", "admin"));

    resource.deleteRoleForCluster(context);
    
    verify(facade).removeRole(eq("admin"), any(ClusterInfo.class));
  }

  @Test
  public void testDeleteRoleForHost() {
    when(request.getValue("corus:role")).thenReturn(new Value("corus:role", "admin"));

    resource.deleteRoleForCluster(context);
    
    verify(facade).removeRole(eq("admin"), any(ClusterInfo.class));
  }

  private void doCheckRole(JSONObject role, int i) {
    assertEquals("role-" + i, role.getString("name"));
    JSONArray permissions = role.getJSONArray("permissions");
    for (int j = 0; j < permissions.size(); j++) {
      JSONObject json = permissions.getJSONObject(j);
      Permission p    = Permission.valueOf(json.getString("name"));
      assertEquals("" + p.abbreviation(), json.getString("abbreviation"));
    }
  }

}
