package org.sapia.corus.ftest.rest;

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
import org.sapia.corus.ftest.FtestClient;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class DistributionResourcesFuncTest {
  
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
    client.getConnector().getDeployerFacade().undeployDistribution(DistributionCriteria.builder().all(), ClusterInfo.clustered());
  }
  
  // --------------------------------------------------------------------------
  // cluster
  
  @Test
  public void testDeployDist_clustered() throws Exception {
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
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity(fis, MediaType.APPLICATION_OCTET_STREAM), JSONValue.class);
      assertEquals(200, response.asObject().getInt("status"));
    }
    
    JSONArray dists = client.resource("/clusters/ftest/distributions")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(client.getHostCount(), dists.size());
  }
  
  @Test
  public void testUndeployDist_clustered() throws Exception {
    File[] matches = client.getConnector().getContext().getFileSystem().getBaseDir().listFiles(
        new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.endsWith("demo.zip");
          }
        }
    );
    assertEquals(1, matches.length, "Could not match");
    client.getConnector().getDeployerFacade().deployDistribution(matches[0].getAbsolutePath(), ClusterInfo.clustered());
    
    JSONValue response = client.resource("/clusters/ftest/distributions")
        .queryParam("d", "*").queryParam("v", "*")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
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
  
  // --------------------------------------------------------------------------
  // specific host

  @Test
  public void testDeployDist_specific_host() throws Exception {
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
      JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral()  + "/distributions")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity(fis, MediaType.APPLICATION_OCTET_STREAM), JSONValue.class);
      assertEquals(200, response.asObject().getInt("status"));
    }
    
    JSONArray dists = client.resource("/clusters/ftest/distributions")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(dists.size(), 1);
  }
  
  @Test
  public void testUndeployDist_specific_host() throws Exception {
    File[] matches = client.getConnector().getContext().getFileSystem().getBaseDir().listFiles(
        new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.endsWith("demo.zip");
          }
        }
    );
    assertEquals(1, matches.length, "Could not match");
    client.getConnector().getDeployerFacade().deployDistribution(matches[0].getAbsolutePath(), ClusterInfo.clustered());
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/distributions")
        .queryParam("d", "*").queryParam("v", "*")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    JSONArray dists = client.resource("/clusters/ftest/hosts/distributions")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(dists.size(), client.getHostCount() - 1);
  }
  
  
  // --------------------------------------------------------------------------
  // security
  
  @Test(expectedExceptions = ForbiddenException.class)
  public void testDeploy_auth_clustered() throws Exception {
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
  
  @Test(expectedExceptions = ForbiddenException.class)
  public void testDeploy_auth_specific_host() throws Exception {
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
      client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/distributions")
        .request()
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity(fis, MediaType.APPLICATION_OCTET_STREAM), JSONValue.class);
    }
  }
 
}
