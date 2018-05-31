package org.sapia.corus.ftest.rest.func;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import net.sf.json.JSONArray;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.Delay;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.corus.client.services.deployer.FileCriteria;
import org.sapia.corus.client.services.deployer.FileInfo;
import org.sapia.corus.ftest.FtestClient;
import org.sapia.corus.ftest.JSONValue;
import org.sapia.corus.ftest.PartitionInfo;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class FileResourcesFuncTest {
  
  static final long DEPLOY_TIMEOUT        = 10000;
  static final long DEPLOY_CHECK_INTERVAL = 2000;
  
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
    client.getConnector().getFileManagementFacade().deleteFiles(FileCriteria.newInstance().setName(ArgMatchers.any()), ClusterInfo.clustered());
  }
  
  // --------------------------------------------------------------------------
  // cluster
  
  @Test
  public void testDeployFile_clustered() throws Exception {
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
      JSONValue response = client.resource("/clusters/ftest/files/demo.zip")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity(fis, MediaType.APPLICATION_OCTET_STREAM), JSONValue.class);
      assertEquals(200, response.asObject().getInt("status"));
    }
 
    waitDeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount());
    
    JSONArray files = client.resource("/clusters/ftest/files/*")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(client.getHostCount(), files.size());
    
  }

  @Test
  public void testUndeployFile_clustered() throws Exception {
    File[] matches = client.getConnector().getContext().getFileSystem().getBaseDir().listFiles(
        new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.endsWith("demo.zip");
          }
        }
    );
    assertEquals(1, matches.length, "Could not match");
    client.getConnector().getDeployerFacade().deployFile(matches[0].getAbsolutePath(), null, DeployPreferences.newInstance(), ClusterInfo.clustered());
    
    waitDeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount());
    
    JSONValue response = client.resource("/clusters/ftest/files/dem*")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUndeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, 0);
    
    JSONArray files = client.resource("/clusters/ftest/files/dem*")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(0, files.size());
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
      JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral()  + "/files/demo.zip")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity(fis, MediaType.APPLICATION_OCTET_STREAM), JSONValue.class);
      assertEquals(200, response.asObject().getInt("status"));
    }
    
    waitDeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, 1);
    
    JSONArray files = client.resource("/clusters/ftest/files")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(files.size(), 1);
  }
  
  @Test
  public void testDeployDist_partition() throws Exception {
    
    PartitionInfo partition = client.createPartitionSet();
    
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
      JSONValue response = client.resource("/clusters/ftest/" + partition  + "/files/demo.zip")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity(fis, MediaType.APPLICATION_OCTET_STREAM), JSONValue.class);
      assertEquals(200, response.asObject().getInt("status"));
    }
    
    waitDeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, 1);
    
    JSONArray files = client.resource("/clusters/ftest/files")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(files.size(), 1);
  }
  
  
  @Test
  public void testUndeployFile_specific_host() throws Exception {
    File[] matches = client.getConnector().getContext().getFileSystem().getBaseDir().listFiles(
        new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.endsWith("demo.zip");
          }
        }
    );
    assertEquals(1, matches.length, "Could not match");
    client.getConnector().getDeployerFacade().deployFile(matches[0].getAbsolutePath(), null, DeployPreferences.newInstance(), ClusterInfo.clustered());
    
    waitDeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount());
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/files/*demo.zip")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUndeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount() - 1);
    
    JSONArray files = client.resource("/clusters/ftest/hosts/files/*")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(files.size(), client.getHostCount() - 1);
  }
  
  @Test
  public void testUndeployFile_partition() throws Exception {
    
    PartitionInfo partition = client.createPartitionSet();
    
    File[] matches = client.getConnector().getContext().getFileSystem().getBaseDir().listFiles(
        new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.endsWith("demo.zip");
          }
        }
    );
    assertEquals(1, matches.length, "Could not match");
    client.getConnector().getDeployerFacade().deployFile(matches[0].getAbsolutePath(), null, DeployPreferences.newInstance(), ClusterInfo.clustered());
    
    waitDeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount());
    
    JSONValue response = client.resource("/clusters/ftest/" + partition + "/files/*demo.zip")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUndeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount() - 1);
    
    JSONArray files = client.resource("/clusters/ftest/hosts/files/*")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(files.size(), client.getHostCount() - 1);
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
      client.resource("/clusters/ftest/files/demo.zip")
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
      client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/files/demo.zip")
        .request()
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity(fis, MediaType.APPLICATION_OCTET_STREAM), JSONValue.class);
    }
  }
  
  private void waitDeployed(long timeout, long pollInterval, int expectedCount)  throws InterruptedException {
    Delay delay = new Delay(timeout, TimeUnit.MILLISECONDS);
    List<FileInfo> files = new ArrayList<FileInfo>();
    while (delay.isNotOver() && files.size() < expectedCount) {
      files.clear();
      Results<List<FileInfo>> results = client.getConnector().getFileManagementFacade().getFiles(FileCriteria.newInstance().setName(ArgMatchers.any()), ClusterInfo.clustered());
      while (results.hasNext()) {
        List<FileInfo> r = results.next().getData();
        files.addAll(r);
      }
      
      if (files.size() >= expectedCount) {
        break;
      }
      Thread.sleep(pollInterval);
    }
   
    assertTrue(files.size() >= expectedCount, "Expected file on " + expectedCount + " hosts. Got: " + files.size());
  }
 
  private void waitUndeployed(long timeout, long pollInterval, int expectedCount)  throws InterruptedException {
    Delay delay = new Delay(timeout, TimeUnit.MILLISECONDS);
    List<FileInfo> files = new ArrayList<FileInfo>();
    while (delay.isNotOver() && files.size() < expectedCount) {
      files.clear();
      Results<List<FileInfo>> results = client.getConnector().getFileManagementFacade().getFiles(FileCriteria.newInstance().setName(ArgMatchers.any()), ClusterInfo.clustered());
      while (results.hasNext()) {
        List<FileInfo> r = results.next().getData();
        files.addAll(r);
      }
      
      if (files.size() >= expectedCount) {
        break;
      }
      Thread.sleep(pollInterval);
    }
   
    assertTrue(files.size() <= expectedCount, "Expected file on " + expectedCount + " hosts. Got: " + files.size());
  }
}
