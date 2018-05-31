package org.sapia.corus.ftest.rest.func;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.common.ArgMatcher;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.IOUtil;
import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.tuple.PairTuple;
import org.sapia.corus.client.services.configurator.Configurator.PropertyScope;
import org.sapia.corus.client.services.configurator.Property;
import org.sapia.corus.ftest.FtestClient;
import org.sapia.corus.ftest.JSONValue;
import org.sapia.corus.ftest.PartitionInfo;
import org.sapia.ubik.util.Collects;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class PropertiesResourcesFuncTest {
  

  private FtestClient client;
  
  private List<Property> expectedJsonPropertiesNoCat;
  private List<Property> expectedJsonPropertiesCat1;
  private List<Property> expectedJsonPropertiesCat2;
  private List<Property> expectedJsonPropertiesGlobalCategory;

  
  @BeforeSuite
  public void beforeSuite() {
    client = FtestClient.open();
   
    expectedJsonPropertiesNoCat = Arrays.asList(
        new Property("test.json.prop4", "value4")
    );
    
    expectedJsonPropertiesCat1 = Arrays.asList(
        new Property("test.json.prop1", "value1"),
        new Property("test.json.prop2", "value2"),
        new Property("test.json.prop3", "value3")
    );
    
    expectedJsonPropertiesCat2 = Arrays.asList(
        new Property("test.json.prop3", "value3")
    );
    
    expectedJsonPropertiesGlobalCategory = Arrays.asList(
        new Property("test.json.prop1", "value1"),
        new Property("test.json.prop2", "value2"),
        new Property("test.json.prop3", "value3"),
        new Property("test.json.prop4", "value4")
    );
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

    assertEquals(checkUrlProperties(results, false), client.getHostCount());
  }
  
  @Test
  public void testAddProperties_clustered_with_json_input() throws Exception {
    JSONValue response = client.resource("/clusters/ftest/properties/process")
        .queryParam("test.prop.1", "value1")
        .queryParam("test.prop.2", "value2")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity(IOUtil.textResourceToString("properties/properties.json"), MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));

    JSONArray results = client.resource("/clusters/ftest/properties/process")
        .queryParam("p", "test.prop.*").request()
        .accept(MediaType.APPLICATION_JSON).get(JSONValue.class).asArray();
    assertEquals(results.size(), client.getHostCount());

    checkUrlProperties(results, false);

    results = client.resource("/clusters/ftest/properties/process")
        .queryParam("p", "test.json.prop*").request()
        .accept(MediaType.APPLICATION_JSON).get(JSONValue.class).asArray();
    
    assertEquals(checkJsonProperties(results, OptionalValue.none(), expectedJsonPropertiesNoCat), client.getHostCount());
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

    assertEquals(checkUrlProperties(results, true), client.getHostCount());
  }
  
  @Test
  public void testAddProperties_clustered_with_json_input_and_specific_categories() throws Exception {
    JSONValue response = client.resource("/clusters/ftest/properties/process")
        .queryParam("test.prop.1", "value1")
        .queryParam("test.prop.2", "value2")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity(IOUtil.textResourceToString("properties/properties.json"), MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));

    JSONArray results = client.resource("/clusters/ftest/properties/process")
        .queryParam("p", "test.prop.*").request()
        .accept(MediaType.APPLICATION_JSON).get(JSONValue.class).asArray();
    assertEquals(results.size(), client.getHostCount());

    checkUrlProperties(results, false);

    results = client.resource("/clusters/ftest/properties/process/cat1")
        .queryParam("p", "test.json.prop*").request()
        .accept(MediaType.APPLICATION_JSON).get(JSONValue.class).asArray();
    
    assertEquals(checkJsonProperties(results, OptionalValue.none(), expectedJsonPropertiesCat1), client.getHostCount());
    
    results = client.resource("/clusters/ftest/properties/process/cat2")
        .queryParam("p", "test.json.prop*").request()
        .accept(MediaType.APPLICATION_JSON).get(JSONValue.class).asArray();
    
    assertEquals(checkJsonProperties(results, OptionalValue.none(), expectedJsonPropertiesCat2), client.getHostCount());
  }
  
  @Test
  public void testAddProperties_category_clustered_with_json_input_and_global_category() throws Exception {
    JSONValue response = client.resource("/clusters/ftest/properties/process/test.category")
        .queryParam("test.prop.1", "value1")
        .queryParam("test.prop.2", "value2")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity(IOUtil.textResourceToString("properties/properties.json"), MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));

    JSONArray results = client
        .resource("/clusters/ftest/properties/process/test.category")
        .queryParam("p", "test.prop.*").request()
        .accept(MediaType.APPLICATION_JSON).get(JSONValue.class).asArray();
    assertEquals(results.size(), client.getHostCount());

    assertEquals(checkUrlProperties(results, true), client.getHostCount());
    
    results = client.resource("/clusters/ftest/properties/process/test.category")
        .queryParam("p", "test.json.prop*").request()
        .accept(MediaType.APPLICATION_JSON).get(JSONValue.class).asArray();
    
    assertEquals(checkJsonProperties(results, OptionalValue.none(), expectedJsonPropertiesGlobalCategory), client.getHostCount());
    
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
  
  @Test
  public void testArchiveProperties_clustered() throws Exception {
    Properties props = new Properties();
    props.setProperty("test.prop.1", "value1");
    props.setProperty("test.prop.2", "value2");
    
    client.getConnector().getConfigFacade().addProperties(
        PropertyScope.PROCESS, 
        props, 
        Collects.arrayToSet("test.category"), 
        false, ClusterInfo.clustered()
     );
    
    JSONValue response = client.resource("/clusters/ftest/properties/archive")
        .queryParam("revId", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
        .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    client.getConnector().getConfigFacade().removeProperty(
        PropertyScope.PROCESS, 
        ArgMatchers.any(), 
        new HashSet<ArgMatcher>(), 
        ClusterInfo.clustered()
     );
    
    response = client.resource("/clusters/ftest/properties/unarchive")
        .queryParam("revId", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
        .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
  
    JSONArray results = client.resource("/clusters/ftest/properties/process/test.category")
        .queryParam("p", "test.prop.*").request()
        .accept(MediaType.APPLICATION_JSON).get(JSONValue.class).asArray();
    assertEquals(results.size(), client.getHostCount());
    
    assertEquals(checkUrlProperties(results, true), client.getHostCount());
  }
  
  // --------------------------------------------------------------------------
  // specific host
  
  @Test
  public void testAddProperties_specific_host() throws Exception {
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/properties/process")
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

    assertEquals(checkUrlProperties(results, true), 1);
  }
  
  @Test
  public void testAddProperties_specific_host_with_json_input() throws Exception {
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/properties/process")
        .queryParam("test.prop.1", "value1")
        .queryParam("test.prop.2", "value2")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity(IOUtil.textResourceToString("properties/properties.json"), MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));

    JSONArray results = client.resource("/clusters/ftest/properties/process")
        .queryParam("p", "test.prop.*").request()
        .accept(MediaType.APPLICATION_JSON).get(JSONValue.class).asArray();
    assertEquals(results.size(), client.getHostCount());

    checkUrlProperties(results, false);

    results = client.resource("/clusters/ftest/properties/process")
        .queryParam("p", "test.json.prop*").request()
        .accept(MediaType.APPLICATION_JSON).get(JSONValue.class).asArray();
    
    assertEquals(checkJsonProperties(results, OptionalValue.none(), expectedJsonPropertiesNoCat), 1);
  }
  
  @Test
  public void testAddProperties_partition() throws Exception {
    
    PartitionInfo partition = client.createPartitionSet();
    
    JSONValue response = client.resource("/clusters/ftest/" + partition + "/properties/process/test.category")
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

    assertEquals(checkUrlProperties(results, true), 1);
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

    assertEquals(checkUrlProperties(results, true), 1);
  }
  
  @Test
  public void testAddProperties_category_partition() throws Exception {
    
    PartitionInfo partition = client.createPartitionSet();
    
    JSONValue response = client.resource("/clusters/ftest/" + partition  + "/properties/process/test.category")
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

    assertEquals(checkUrlProperties(results, true), 1);
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
    
    assertEquals(checkUrlProperties(results, true), client.getHostCount() - 1);
  }
  
  @Test
  public void testDeleteProperties_category_partition() throws Exception {
    
    PartitionInfo partition = client.createPartitionSet();
    
    Properties props = new Properties();
    props.setProperty("test.prop.1", "value1");
    props.setProperty("test.prop.2", "value2");
    
    client.getConnector().getConfigFacade().addProperties(
        PropertyScope.PROCESS, 
        props, 
        Collects.arrayToSet("test.category"), 
        false, ClusterInfo.clustered()
     );
    
    JSONValue response = client.resource("/clusters/ftest/" + partition + "/properties/process/test.category")
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
    
    assertEquals(checkUrlProperties(results, true), client.getHostCount() - 1);
  }
  
  @Test
  public void testArchiveProperties_partition() throws Exception {
    
    PartitionInfo partition = client.createPartitionSet();
    
    Properties props = new Properties();
    props.setProperty("test.prop.1", "value1");
    props.setProperty("test.prop.2", "value2");
    
    client.getConnector().getConfigFacade().addProperties(
        PropertyScope.PROCESS, 
        props, 
        Collects.arrayToSet("test.category"), 
        false, ClusterInfo.clustered()
     );
    
    JSONValue response = client.resource("/clusters/ftest/" + partition + "/properties/archive")
        .queryParam("revId", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
        .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    response = client.resource("/clusters/ftest/properties/process/test.category")
        .queryParam("p", "test.prop.*")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    response = client.resource("/clusters/ftest/" + partition + "/properties/unarchive")
        .queryParam("revId", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
        .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
  
    JSONArray results = client.resource("/clusters/ftest/properties/process/test.category")
        .queryParam("p", "test.prop.*").request()
        .accept(MediaType.APPLICATION_JSON).get(JSONValue.class).asArray();
    assertEquals(results.size(), client.getHostCount());
    
    assertEquals(checkUrlProperties(results, true), 1);
  }
 
  // --------------------------------------------------------------------------
  // auth
  
  private int checkUrlProperties(JSONArray results, boolean checkCategory) {
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
  
  private int checkJsonProperties(JSONArray results, OptionalValue<String> categoryName, List<Property> expected) {
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
        props.setProperty(jsonProp.getString("name"), jsonProp.getString("value"));
        categoryName.ifSet(c -> {
          assertEquals(jsonProp.getString("category"), c);
        });
      }
      assertEquals(props.size(), expected.size());
      
      expected.forEach(pt -> {
        assertEquals(props.getProperty(pt.getName()), pt.getValue()); 
      });
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
