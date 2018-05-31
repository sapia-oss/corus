package org.sapia.corus.ftest.rest.func;

import static org.testng.Assert.assertEquals;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import net.sf.json.JSONArray;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.services.security.Permission;
import org.sapia.corus.ftest.FtestClient;
import org.sapia.corus.ftest.JSONValue;
import org.sapia.corus.ftest.PartitionInfo;
import org.sapia.ubik.util.Collects;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class RoleResourcesFuncTest {
  
  private FtestClient client;
  
  @BeforeSuite
  public void beforeSuite() {
    client = FtestClient.open();
  }
  
  @AfterSuite
  public void afterSuite() throws Exception {
    tearDown();
    client.close();
  }
  
  @BeforeMethod
  public void beforeMethod() throws Exception {
    tearDown();
  }
  
  private void tearDown() {
    client.getConnector().getSecurityManagementFacade().removeRole(ArgMatchers.parse("test.*"), ClusterInfo.clustered());
  }

  // --------------------------------------------------------------------------
  // clustered
  
  @Test
  public void testAddRole_clustered() throws Exception {
    JSONValue response = client.resource("/clusters/ftest/roles/test.role")
        .queryParam("permissions", "rwx")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray roles = client.resource("/clusters/ftest/roles")
        .queryParam("n", "test.*")
        .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    assertEquals(roles.size(), client.getHostCount());
  }
  
  @Test
  public void testDeleteRole_clustered() throws Exception {
    client.getConnector().getSecurityManagementFacade().addRole("test.role", Collects.arrayToSet(Permission.values()), ClusterInfo.clustered());
    
    JSONValue response = client.resource("/clusters/ftest/roles/test.role")
        .queryParam("permissions", "rwx")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray roles = client.resource("/clusters/ftest/roles")
        .queryParam("n", "test.*")
        .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    assertEquals(roles.size(), 0);
  }
  
  // --------------------------------------------------------------------------
  // specific host
  
  @Test
  public void testAddRole_specific_host() throws Exception {
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/roles/test.role")
        .queryParam("permissions", "rwx")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray roles = client.resource("/clusters/ftest/roles")
        .queryParam("n", "test.*")
        .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    assertEquals(roles.size(), 1);
  }

  @Test
  public void testAddRole_partition() throws Exception {
    
    PartitionInfo partition = client.createPartitionSet();
    
    JSONValue response = client.resource("/clusters/ftest/" + partition  + "/roles/test.role")
        .queryParam("permissions", "rwx")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray roles = client.resource("/clusters/ftest/roles")
        .queryParam("n", "test.*")
        .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    assertEquals(roles.size(), 1);
  }
  
  @Test
  public void testDeleteRole_specific_host() throws Exception {
    client.getConnector().getSecurityManagementFacade().addRole("test.role", Collects.arrayToSet(Permission.values()), ClusterInfo.clustered());
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/roles/test.role")
        .queryParam("permissions", "rwx")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray roles = client.resource("/clusters/ftest/roles")
        .queryParam("n", "test.*")
        .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    assertEquals(roles.size(), client.getHostCount() - 1);
  }
  
  @Test
  public void testDeleteRole_partition() throws Exception {
    
    PartitionInfo partition = client.createPartitionSet();
    
    client.getConnector().getSecurityManagementFacade().addRole("test.role", Collects.arrayToSet(Permission.values()), ClusterInfo.clustered());
    
    JSONValue response = client.resource("/clusters/ftest/" + partition + "/roles/test.role")
        .queryParam("permissions", "rwx")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray roles = client.resource("/clusters/ftest/roles")
        .queryParam("n", "test.*")
        .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    assertEquals(roles.size(), client.getHostCount() - 1);
  }
}
