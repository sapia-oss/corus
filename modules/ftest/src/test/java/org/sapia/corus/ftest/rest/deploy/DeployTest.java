package org.sapia.corus.ftest.rest.deploy;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import net.sf.json.JSONArray;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.ftest.JSONValue;
import org.sapia.corus.ftest.RestClient;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class DeployTest {
  
  private RestClient client;
  
  @BeforeSuite
  public void setUp() {
    client = RestClient.open();
  }
  
  @AfterSuite
  public void tearDown() throws Exception {
    client.getConnector().getDeployerFacade().undeployDistribution(DistributionCriteria.builder().all(), ClusterInfo.clustered());
    client.close();
  }
  
  @Test
  public void testDeployDist() throws Exception {
    File[] matches = client.getConnector().getContext().getFileSystem().getBaseDir().listFiles(
        new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.endsWith("demo.zip");
          }
        }
    );
    
    assertEquals(1, matches.length, "Could not match");
    
    try(FileInputStream fis = new FileInputStream(matches[0])) {
      JSONValue response = client.resource("/clusters/ftest/distributions")
        .request()
          .header(RestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(RestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity(fis, MediaType.APPLICATION_OCTET_STREAM), JSONValue.class);
      assertEquals(200, response.asObject().getInt("status"));
    }
    
    JSONArray dists = client.resource("/clusters/ftest/distributions")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(1, dists.size());
  }
 
  @Test(dependsOnMethods = "testDeployDist")
  public void testUndeployDist() throws Exception {
    JSONValue response = client.resource("/clusters/ftest/distributions")
        .queryParam("d", "*").queryParam("v", "*")
        .request()
          .header(RestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(RestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray dists = client.resource("/clusters/ftest/distributions")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(0, dists.size());
  }
  
  @Test(expectedExceptions = ForbiddenException.class)
  public void testUnauthDeploy() throws Exception {
    File[] matches = client.getConnector().getContext().getFileSystem().getBaseDir().listFiles(
        new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.endsWith("demo.zip");
          }
        }
    );
    
    assertEquals(1, matches.length, "Could not match");
    
    try(FileInputStream fis = new FileInputStream(matches[0])) {
      client.resource("/clusters/ftest/distributions")
        .request()
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity(fis, MediaType.APPLICATION_OCTET_STREAM), JSONValue.class);
    }
  }
 
}
