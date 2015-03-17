package org.sapia.corus.ftest.rest;

import static org.testng.Assert.assertEquals;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import net.sf.json.JSONArray;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.ftest.FtestClient;
import org.sapia.corus.ftest.JSONValue;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class PortResourcesFunctionalTest {

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
  
  private void tearDown() throws Exception {
    client.getConnector().getPortManagementFacade().removePortRange("test.port", true, ClusterInfo.clustered());
  }

  // --------------------------------------------------------------------------
  // clustered
  
  @Test
  public void testAddPortRange_clustered() throws Exception {
    
    JSONValue response = client.resource("/clusters/ftest/ports/ranges/test.port")
        .queryParam("min", "100")
        .queryParam("max", "105")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray ranges = client.resource("/clusters/ftest/ports/ranges")
        .queryParam("n", "test.*")
        .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    assertEquals(ranges.size(), client.getHostCount());
    
  }
  
  @Test
  public void testDeletePortRange_clustered() throws Exception {
    
    client.getConnector().getPortManagementFacade().addPortRange("test.port", 100, 105, ClusterInfo.clustered());
    
    JSONValue response = client.resource("/clusters/ftest/ports/ranges/test.port")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray ranges = client.resource("/clusters/ftest/ports/ranges")
        .queryParam("n", "test.*")
        .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    assertEquals(ranges.size(), 0);
    
  }
  
  @Test
  public void testReleasePortRange_clustered() throws Exception {
    
    JSONValue response = client.resource("/clusters/ftest/ports/ranges/test.port")
        .queryParam("min", "100")
        .queryParam("max", "105")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
  }
  
  @Test
  public void testArchivePortRange_cluster() throws Exception {
    
    client.getConnector().getPortManagementFacade().addPortRange("test.port", 100, 105, ClusterInfo.clustered());

    JSONValue response = client.resource("/clusters/ftest/ports/ranges/archive")
        .queryParam("revId", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    client.getConnector().getPortManagementFacade().removePortRange("test.port", false, ClusterInfo.clustered());
    
    response = client.resource("/clusters/ftest/ports/ranges/unarchive")
        .queryParam("revId", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray ranges = client.resource("/clusters/ftest/ports/ranges")
        .queryParam("n", "test.*")
        .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    assertEquals(ranges.size(), client.getHostCount());
    
  }
  
  // --------------------------------------------------------------------------
  // specific host
  
  @Test
  public void testAddPortRange_specific_host() throws Exception {
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/ports/ranges/test.port")
        .queryParam("min", "100")
        .queryParam("max", "105")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray ranges = client.resource("/clusters/ftest/ports/ranges")
        .queryParam("n", "test.*")
        .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    assertEquals(ranges.size(), 1);
    
  }
  
  @Test
  public void testDeletePortRange_specific_host() throws Exception {
    
    client.getConnector().getPortManagementFacade().addPortRange("test.port", 100, 105, ClusterInfo.clustered());
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/ports/ranges/test.port")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray ranges = client.resource("/clusters/ftest/ports/ranges")
        .queryParam("n", "test.*")
        .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    assertEquals(ranges.size(), client.getHostCount() - 1);
    
  }
  
  @Test
  public void testReleasePortRange_specific_host() throws Exception {
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/ports/ranges/test.port")
        .queryParam("min", "100")
        .queryParam("max", "105")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
  }
  
  @Test
  public void testArchivePortRange_specific_host() throws Exception {
    
    client.getConnector().getPortManagementFacade().addPortRange("test.port", 100, 105, ClusterInfo.clustered());

    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/ports/ranges/archive")
        .queryParam("revId", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    client.getConnector().getPortManagementFacade().removePortRange("test.port", false, ClusterInfo.clustered());
    
    response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/ports/ranges/unarchive")
        .queryParam("revId", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray ranges = client.resource("/clusters/ftest/ports/ranges")
        .queryParam("n", "test.*")
        .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    assertEquals(ranges.size(), 1);
    
  }
}
