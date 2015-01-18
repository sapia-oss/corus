package org.sapia.corus.ftest.rest;

import static org.testng.Assert.assertEquals;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.services.security.Permission;
import org.sapia.corus.ftest.FtestClient;
import org.sapia.corus.ftest.JSONValue;
import org.sapia.ubik.util.Collects;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class ApplicationKeyResourcesFuncTest {
  
  private FtestClient client;
  
  @BeforeSuite
  public void beforeSuite() {
    client = FtestClient.open();
    client.getConnector().getSecurityManagementFacade().addOrUpdateRole("test.role", Collects.arrayToSet(Permission.values()), ClusterInfo.clustered());
    client.getConnector().getSecurityManagementFacade().addOrUpdateRole("test.role2", Collects.arrayToSet(Permission.values()), ClusterInfo.clustered());

  }
  
  @AfterSuite
  public void afterSuite() throws Exception {
    tearDown();
    client.getConnector().getSecurityManagementFacade().removeRole(ArgFactory.parse("test.rol*"), ClusterInfo.clustered());
    client.close();
  }
  
  @BeforeMethod
  public void beforeMethod() throws Exception {
    tearDown();
  }
  
  private void tearDown() {
    client.getConnector().getApplicationKeyManagementFacade().removeAppKey(ArgFactory.parse("test.*"), ClusterInfo.clustered());
  }

  // --------------------------------------------------------------------------
  // clustered
  
  @Test
  public void testCreateAppKey_clustered() throws Exception {
    
    JSONValue response = client.resource("/clusters/ftest/appkeys/test.app")
        .queryParam("k", "1234")
        .queryParam("r", "test.role")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray appkeys = client.resource("/clusters/ftest/appkeys")
        .queryParam("a", "test.*")
        .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    assertEquals(appkeys.size(), client.getHostCount());
    
  }
  
  @Test
  public void testUpdateKey_clustered() throws Exception {
    
    client.getConnector().getApplicationKeyManagementFacade().createApplicationKey("test.app", "1234", "test.role", ClusterInfo.clustered());
    
    JSONValue response = client.resource("/clusters/ftest/appkeys/test.app/key/3456")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray appkeys = client.resource("/clusters/ftest/appkeys")
        .queryParam("a", "test.*")
        .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    assertEquals(appkeys.size(), client.getHostCount());
    
    for (int i = 0; i < appkeys.size(); i++) {
      JSONObject key = appkeys.getJSONObject(i).getJSONObject("data");
      assertEquals(key.getString("key"), "3456");
    }
    
  }
  
  @Test
  public void testUpdateRole_clustered() throws Exception {
    
    client.getConnector().getApplicationKeyManagementFacade().createApplicationKey("test.app", "1234", "test.role", ClusterInfo.clustered());
    
    JSONValue response = client.resource("/clusters/ftest/appkeys/test.app/role/test.role2")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray appkeys = client.resource("/clusters/ftest/appkeys")
        .queryParam("a", "test.*")
        .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    assertEquals(appkeys.size(), client.getHostCount());
    
    for (int i = 0; i < appkeys.size(); i++) {
      JSONObject key = appkeys.getJSONObject(i).getJSONObject("data");
      assertEquals(key.getString("role"), "test.role2");
    }
    
  }
  
  @Test
  public void testDeleteAppKey_clustered() throws Exception {
    
    client.getConnector().getApplicationKeyManagementFacade().createApplicationKey("test.app", "1234", "test.role", ClusterInfo.clustered());
    
    JSONValue response = client.resource("/clusters/ftest/appkeys/test.app")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray appkeys = client.resource("/clusters/ftest/appkeys")
        .queryParam("a", "test.*")
        .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    assertEquals(appkeys.size(), 0);
    
  }
  
  // --------------------------------------------------------------------------
  // specific host
  
  @Test
  public void testCreateAppKey_specific_host() throws Exception {
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/appkeys/test.app")
        .queryParam("k", "1234")
        .queryParam("r", "test.role")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray appkeys = client.resource("/clusters/ftest/appkeys")
        .queryParam("a", "test.*")
        .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    assertEquals(appkeys.size(), 1);
    
  }
  
  @Test
  public void testUpdateKey_specific_host() throws Exception {
    
    client.getConnector().getApplicationKeyManagementFacade().createApplicationKey("test.app", "1234", "test.role", ClusterInfo.clustered());
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral()  + "/appkeys/test.app/key/3456")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray appkeys = client.resource("/clusters/ftest/appkeys")
        .queryParam("a", "test.*")
        .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    assertEquals(appkeys.size(), client.getHostCount());
    
    boolean updatedFound = false;
    for (int i = 0; i < appkeys.size(); i++) {
      JSONObject key = appkeys.getJSONObject(i).getJSONObject("data");
      if (key.getString("key").equals("3456")) {
        if (updatedFound) {
          throw new IllegalStateException("Response should have only one key updated to the new value");
        }
        updatedFound = true;
      }
    }
    
    if (!updatedFound) {
      throw new IllegalStateException("Could not find updated value in response");
    }
    
  }
  
  @Test
  public void testUpdateRole_specific_host() throws Exception {
    
    client.getConnector().getApplicationKeyManagementFacade().createApplicationKey("test.app", "1234", "test.role", ClusterInfo.clustered());
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral()  + "/appkeys/test.app/role/test.role2")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray appkeys = client.resource("/clusters/ftest/appkeys")
        .queryParam("a", "test.*")
        .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    assertEquals(appkeys.size(), client.getHostCount());
    
    boolean updatedFound = false;
    for (int i = 0; i < appkeys.size(); i++) {
      JSONObject key = appkeys.getJSONObject(i).getJSONObject("data");
      if (key.getString("role").equals("test.role2")) {
        if (updatedFound) {
          throw new IllegalStateException("Response should have only one key updated to the new value");
        }
        updatedFound = true;
      }
    }
    
    if (!updatedFound) {
      throw new IllegalStateException("Could not find updated value in response");
    }
    
  }
  
  @Test
  public void testDeleteAppKey_specific_host() throws Exception {
    
    client.getConnector().getApplicationKeyManagementFacade().createApplicationKey("test.app", "1234", "test.role", ClusterInfo.clustered());
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/appkeys/test.app")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray appkeys = client.resource("/clusters/ftest/appkeys")
        .queryParam("a", "test.*")
        .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    assertEquals(appkeys.size(), client.getHostCount() - 1);
    
  }
}
