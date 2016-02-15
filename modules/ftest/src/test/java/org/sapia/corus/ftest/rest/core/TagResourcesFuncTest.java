package org.sapia.corus.ftest.rest.core;

import static org.junit.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.ftest.FtestClient;
import org.sapia.corus.ftest.JSONValue;
import org.sapia.corus.ftest.PartitionInfo;
import org.sapia.ubik.util.Collects;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class TagResourcesFuncTest {

  private FtestClient client;
  
  @BeforeSuite
  public void beforeSuite() {
    client = FtestClient.open();
  }
  
  @AfterSuite
  public void afterSuite() {
    beforeTest();
    client.close();
  }
  
  @BeforeMethod
  public void beforeTest() {
    client.getConnector().getConfigFacade().removeTag("test.tag", ClusterInfo.clustered());
  }
  
  // --------------------------------------------------------------------------
  // cluster
  
  @Test
  public void testAddTag_clustered() throws Exception {
    
    JSONValue response = client.resource("/clusters/ftest/tags/test.tag")
      .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON) 
        .put(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray results = client.resource("/clusters/ftest/tags")
        .queryParam("t", "test.*")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(results.size(), client.getHostCount());
    
    List<String> tagList = new ArrayList<>();
    for (int i = 0; i < results.size(); i++) {
      JSONObject result = results.getJSONObject(i);
      JSONArray tags = result.getJSONArray("data");
      for (int j = 0; j < tags.size(); j++) {
        tagList.add(tags.getString(j));
      }
    }
    assertEquals(tagList.size(), client.getHostCount());
    assertTrue(tagList.containsAll(Collects.arrayToList("test.tag")));
  }
  
  @Test
  public void testArchiveUnarchiveTags_clustered() throws Exception {
    
    client.getConnector().getConfigFacade().addTag("test.tag", ClusterInfo.clustered());
    
    JSONValue response = client.resource("/clusters/ftest/tags/archive")
      .queryParam("revId", "test")
      .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON) 
        .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    client.getConnector().getConfigFacade().removeTag("test.tag", ClusterInfo.clustered());

    response = client.resource("/clusters/ftest/tags/unarchive")
        .queryParam("revId", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
      assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray results = client.resource("/clusters/ftest/tags")
        .queryParam("t", "test.*")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(results.size(), client.getHostCount());
    
    List<String> tagList = new ArrayList<>();
    for (int i = 0; i < results.size(); i++) {
      JSONObject result = results.getJSONObject(i);
      JSONArray tags = result.getJSONArray("data");
      for (int j = 0; j < tags.size(); j++) {
        tagList.add(tags.getString(j));
      }
    }
    assertEquals(tagList.size(), client.getHostCount());
    assertTrue(tagList.containsAll(Collects.arrayToList("test.tag")));
  }
  
  @Test
  public void testDeleteTag_clustered() throws Exception {
        
    JSONValue response = client.resource("/clusters/ftest/tags/test.tag")
      .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON) 
        .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray results = client.resource("/clusters/ftest/tags")
        .queryParam("t", "test.*")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
 
    List<String> tagList = new ArrayList<>();
    for (int i = 0; i < results.size(); i++) {
      JSONObject result = results.getJSONObject(i);
      JSONArray tags = result.getJSONArray("data");
      for (int j = 0; j < tags.size(); j++) {
        tagList.add(tags.getString(j));
      }
    }
    assertEquals(0, tagList.size());
  }
  
  // --------------------------------------------------------------------------
  // specific host
  
  @Test
  public void testAddTag_specific_host() throws Exception {
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral()  + "/tags/test.tag")
      .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON) 
        .put(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
      assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray results = client.resource("/clusters/ftest/tags")
        .queryParam("t", "test.*")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(results.size(), client.getHostCount());
    
    List<String> tagList = new ArrayList<>();
    for (int i = 0; i < results.size(); i++) {
      JSONObject result = results.getJSONObject(i);
      JSONArray tags = result.getJSONArray("data");
      for (int j = 0; j < tags.size(); j++) {
        tagList.add(tags.getString(j));
      }
    }
    assertEquals(tagList.size(), 1);
    assertTrue(tagList.containsAll(Collects.arrayToList("test.tag")));
  }
  
  @Test
  public void testAddTag_partition() throws Exception {
    
    PartitionInfo partition = client.createPartitionSet();
    
    JSONValue response = client.resource("/clusters/ftest/" + partition  + "/tags/test.tag")
      .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON) 
        .put(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
      assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray results = client.resource("/clusters/ftest/tags")
        .queryParam("t", "test.*")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(results.size(), client.getHostCount());
    
    List<String> tagList = new ArrayList<>();
    for (int i = 0; i < results.size(); i++) {
      JSONObject result = results.getJSONObject(i);
      JSONArray tags = result.getJSONArray("data");
      for (int j = 0; j < tags.size(); j++) {
        tagList.add(tags.getString(j));
      }
    }
    assertEquals(tagList.size(), 1);
    assertTrue(tagList.containsAll(Collects.arrayToList("test.tag")));
  }
  
  @Test
  public void testDeleteTag_specific_host() throws Exception {
    
    client.getConnector().getConfigFacade().addTag("test.tag", ClusterInfo.clustered());
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/tags/test.tag")
      .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON) 
        .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray results = client.resource("/clusters/ftest/tags")
        .queryParam("t", "test.*")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
 
    List<String> tagList = new ArrayList<>();
    for (int i = 0; i < results.size(); i++) {
      JSONObject result = results.getJSONObject(i);
      JSONArray tags = result.getJSONArray("data");
      for (int j = 0; j < tags.size(); j++) {
        tagList.add(tags.getString(j));
      }
    }
    assertEquals(tagList.size(), 1);
  }
  
  @Test
  public void testDeleteTag_partition() throws Exception {
    
    
    PartitionInfo partition = client.createPartitionSet();
    
    client.getConnector().getConfigFacade().addTag("test.tag", ClusterInfo.clustered());
    
    JSONValue response = client.resource("/clusters/ftest/" + partition + "/tags/test.tag")
      .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON) 
        .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray results = client.resource("/clusters/ftest/tags")
        .queryParam("t", "test.*")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
 
    List<String> tagList = new ArrayList<>();
    for (int i = 0; i < results.size(); i++) {
      JSONObject result = results.getJSONObject(i);
      JSONArray tags = result.getJSONArray("data");
      for (int j = 0; j < tags.size(); j++) {
        tagList.add(tags.getString(j));
      }
    }
    assertEquals(tagList.size(), 1);
  }
  
  @Test
  public void testArchiveUnarchiveTags_specific_host() throws Exception {
    
    client.getConnector().getConfigFacade().addTag("test.tag", ClusterInfo.clustered());
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/tags/archive")
      .queryParam("revId", "test")
      .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON) 
        .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    client.getConnector().getConfigFacade().removeTag("test.tag", ClusterInfo.clustered());

    response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/tags/unarchive")
        .queryParam("revId", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
      assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray results = client.resource("/clusters/ftest/tags")
        .queryParam("t", "test.*")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(results.size(), client.getHostCount());
    
    List<String> tagList = new ArrayList<>();
    for (int i = 0; i < results.size(); i++) {
      JSONObject result = results.getJSONObject(i);
      JSONArray tags = result.getJSONArray("data");
      for (int j = 0; j < tags.size(); j++) {
        tagList.add(tags.getString(j));
      }
    }
    assertEquals(tagList.size(), 1);
    assertTrue(tagList.containsAll(Collects.arrayToList("test.tag")));
  }
  
  @Test
  public void testArchiveUnarchiveTags_partition() throws Exception {
    
    PartitionInfo partition = client.createPartitionSet();
    
    client.getConnector().getConfigFacade().addTag("test.tag", ClusterInfo.clustered());
    
    JSONValue response = client.resource("/clusters/ftest/" + partition + "/tags/archive")
      .queryParam("revId", "test")
      .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON) 
        .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    client.getConnector().getConfigFacade().removeTag("test.tag", ClusterInfo.clustered());

    response = client.resource("/clusters/ftest/" + partition + "/tags/unarchive")
        .queryParam("revId", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
      assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray results = client.resource("/clusters/ftest/tags")
        .queryParam("t", "test.*")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(results.size(), client.getHostCount());
    
    List<String> tagList = new ArrayList<>();
    for (int i = 0; i < results.size(); i++) {
      JSONObject result = results.getJSONObject(i);
      JSONArray tags = result.getJSONArray("data");
      for (int j = 0; j < tags.size(); j++) {
        tagList.add(tags.getString(j));
      }
    }
    assertEquals(tagList.size(), 1);
    assertTrue(tagList.containsAll(Collects.arrayToList("test.tag")));
  }
  
  // --------------------------------------------------------------------------
  // security
  
  @Test(expectedExceptions = ForbiddenException.class)
  public void testAddTag_auth_clustered() throws Exception {
    client.resource("/clusters/ftest/tags/test.tag")
      .request()
        .accept(MediaType.APPLICATION_JSON) 
        .put(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
  }
  
  @Test(expectedExceptions = ForbiddenException.class)
  public void testAddTag_auth_specific_host() throws Exception {
    client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/tags/test.tag")
      .request()
        .accept(MediaType.APPLICATION_JSON) 
        .put(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
  }
}
