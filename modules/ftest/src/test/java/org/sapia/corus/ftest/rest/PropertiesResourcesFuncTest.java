package org.sapia.corus.ftest.rest;

import static org.testng.Assert.assertEquals;

import java.util.HashSet;
import java.util.Properties;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.ftest.FtestClient;
import org.sapia.corus.ftest.JSONValue;
import org.sapia.ubik.util.Collects;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class PropertiesResourcesFuncTest {

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
    client.getConnector().getConfigFacade().removeProperty( 
        PropertyScope.PROCESS, 
        ArgMatchers.parse("test.prop.*"),
        new HashSet<ArgMatcher>(),
        ClusterInfo.clustered()
    );
    
    client.getConnector().getConfigFacade().removeProperty( 
        PropertyScope.PROCESS, 
        ArgMatchers.parse("test.prop.*"),
        Collects.arrayToSet(ArgMatchers.parse("test.*")),
        ClusterInfo.clustered()
    );
  }
  
  // --------------------------------------------------------------------------
  // cluster
  
  @Test
  public void testAddProperties_clustered() throws Exception {
    JSONValue response = client.resource("/clusters/ftest/properties/process")
        .queryParam("test.prop.1", "value1")
        .queryParam("test.prop.2", "value2")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));

    JSONArray results = client.resource("/clusters/ftest/properties/process")
        .queryParam("p", "test.prop.*").request()
        .accept(MediaType.APPLICATION_JSON).get(JSONValue.class).asArray();
    assertEquals(results.size(), client.getHostCount());

    checkProperties(results, false);
  }
  
  @Test
  public void testDeleteProperties_clustered() throws Exception {
    Properties props = new Properties();
    props.setProperty("test.prop.1", "value1");
    props.setProperty("test.prop.2", "value2");
    
    client.getConnector().getConfigFacade().addProperties(PropertyScope.PROCESS, props, new HashSet<String>(), false, ClusterInfo.clustered());
    
    JSONValue response = client.resource("/clusters/ftest/properties/process")
        .queryParam("p", "test.prop.*")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));

    JSONArray results = client.resource("/clusters/ftest/properties/process")
        .queryParam("p", "test.prop.*").request()
        .accept(MediaType.APPLICATION_JSON).get(JSONValue.class).asArray();
    assertEquals(results.size(), client.getHostCount());
    checkEmptyProperties(results);
  }
  
  @Test
  public void testAddProperties_category_clustered() throws Exception {
    JSONValue response = client.resource("/clusters/ftest/properties/process/test.category")
        .queryParam("test.prop.1", "value1")
        .queryParam("test.prop.2", "value2")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));

    JSONArray results = client
        .resource("/clusters/ftest/properties/process/test.category")
        .queryParam("p", "test.prop.*").request()
        .accept(MediaType.APPLICATION_JSON).get(JSONValue.class).asArray();
    assertEquals(results.size(), client.getHostCount());

    assertEquals(checkProperties(results, true), client.getHostCount());
  }
  
  @Test
  public void testDeleteProperties_category_clustered() throws Exception {
    Properties props = new Properties();
    props.setProperty("test.prop.1", "value1");
    props.setProperty("test.prop.2", "value2");
    
    client.getConnector().getConfigFacade().addProperties(
        PropertyScope.PROCESS, 
        props, 
        Collects.arrayToSet("test.category"), 
        false, ClusterInfo.clustered()
     );
    
    JSONValue response = client.resource("/clusters/ftest/properties/process/test.category")
        .queryParam("p", "test.prop.*")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));

    JSONArray results = client.resource("/clusters/ftest/properties/process/test.category")
        .queryParam("p", "test.prop.*").request()
        .accept(MediaType.APPLICATION_JSON).get(JSONValue.class).asArray();
    assertEquals(results.size(), client.getHostCount());
    checkEmptyProperties(results);
  }
  
  // --------------------------------------------------------------------------
  // specific host
  
  @Test
  public void testAddProperties_specific_host() throws Exception {
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/properties/process/test.category")
        .queryParam("test.prop.1", "value1")
        .queryParam("test.prop.2", "value2")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));

    JSONArray results = client.resource("/clusters/ftest/properties/process")
        .queryParam("p", "test.prop.*").request()
        .accept(MediaType.APPLICATION_JSON).get(JSONValue.class).asArray();
    assertEquals(results.size(), client.getHostCount());

    assertEquals(checkProperties(results, true), 1);
  }
  
  @Test
  public void testAddProperties_category_specific_host() throws Exception {
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/properties/process/test.category")
        .queryParam("test.prop.1", "value1")
        .queryParam("test.prop.2", "value2")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));

    JSONArray results = client
        .resource("/clusters/ftest/properties/process/test.category")
        .queryParam("p", "test.prop.*").request()
        .accept(MediaType.APPLICATION_JSON).get(JSONValue.class).asArray();
    assertEquals(results.size(), client.getHostCount());

    assertEquals(checkProperties(results, true), 1);
  }
  
  @Test
  public void testDeleteProperties_category_specific_host() throws Exception {
    Properties props = new Properties();
    props.setProperty("test.prop.1", "value1");
    props.setProperty("test.prop.2", "value2");
    
    client.getConnector().getConfigFacade().addProperties(
        PropertyScope.PROCESS, 
        props, 
        Collects.arrayToSet("test.category"), 
        false, ClusterInfo.clustered()
     );
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/properties/process/test.category")
        .queryParam("p", "test.prop.*")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));

    JSONArray results = client.resource("/clusters/ftest/properties/process/test.category")
        .queryParam("p", "test.prop.*").request()
        .accept(MediaType.APPLICATION_JSON).get(JSONValue.class).asArray();
    assertEquals(results.size(), client.getHostCount());
    
    assertEquals(checkProperties(results, true), client.getHostCount() - 1);
  }
 
  // --------------------------------------------------------------------------
  // auth
  
  private int checkProperties(JSONArray results, boolean checkCategory) {
    int totalNonEmpty = 0;
    for (int i = 0; i < results.size(); i++) {
      JSONObject result = results.getJSONObject(i);
      JSONArray jsonProps = result.getJSONArray("data");
      if (jsonProps.size() > 0) {
        totalNonEmpty++;
      }
      Properties props = new Properties();
      for (int j = 0; j < jsonProps.size(); j++) {
        JSONObject jsonProp = jsonProps.getJSONObject(j);
        props.setProperty(jsonProp.getString("name"),
            jsonProp.getString("value"));
        if (checkCategory) {
          assertEquals(jsonProp.getString("category"), "test.category");
        }
      }
      if (jsonProps.size() > 0) {
        assertEquals(jsonProps.size(), 2);
        assertEquals(props.getProperty("test.prop.1"), "value1");
        assertEquals(props.getProperty("test.prop.2"), "value2");
      }
    }
    return totalNonEmpty;
  }
  
  private void checkEmptyProperties(JSONArray results) {
    for (int i = 0; i < results.size(); i++) {
      JSONObject result = results.getJSONObject(i);
      JSONArray jsonProps = result.getJSONArray("data");
      assertEquals(jsonProps.size(), 0);
    }
  }
}
