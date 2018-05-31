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

import org.apache.commons.lang.text.StrLookup;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.Interpreter;
import org.sapia.corus.client.common.Delay;
import org.sapia.corus.client.services.database.RevId;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.UndeployPreferences;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.ftest.FtestClient;
import org.sapia.corus.ftest.JSONValue;
import org.sapia.corus.ftest.PartitionInfo;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class DistributionResourcesFuncTest {
  
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
    Interpreter interp = new Interpreter(client.getConnector());
    try {
      interp.eval("kill -d demo -v * -n noopApp -w 60 -cluster", StrLookup.noneLookup());
    } catch (Throwable err) {
      throw new IllegalStateException("Could not kill processes", err);
    }

    client.getConnector().getDeployerFacade().unarchiveDistributions(RevId.valueOf("previous"), ClusterInfo.clustered());
    client.getConnector().getDeployerFacade().undeployDistribution(DistributionCriteria.builder().all(), UndeployPreferences.newInstance(), ClusterInfo.clustered());
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
 
    waitDeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount());
    
    JSONArray dists = client.resource("/clusters/ftest/distributions")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(client.getHostCount(), dists.size());
    
  }
  
  @Test
  public void testDeployDist_clustered_rippled() throws Exception {
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
        .queryParam("minHosts", "1")
        .queryParam("batchSize", "1")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity(fis, MediaType.APPLICATION_OCTET_STREAM), JSONValue.class);
      assertEquals(response.asObject().getInt("status"), 200);
    }
 
    waitDeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount());
    
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
    client.getConnector().getDeployerFacade().deployDistribution(matches[0].getAbsolutePath(), DeployPreferences.newInstance(), ClusterInfo.clustered());
    
    waitDeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount());
    
    JSONValue response = client.resource("/clusters/ftest/distributions")
        .queryParam("d", "*").queryParam("v", "*")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUndeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, 0);
    
    JSONArray dists = client.resource("/clusters/ftest/distributions")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(0, dists.size());
  }
  
  @Test
  public void testArchiveUnarchiveDist_clustered() throws Exception {
    File[] matches = client.getConnector().getContext().getFileSystem().getBaseDir().listFiles(
        new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.endsWith("demo.zip");
          }
        }
    );
    assertEquals(1, matches.length, "Could not match");
    client.getConnector().getDeployerFacade().deployDistribution(matches[0].getAbsolutePath(), DeployPreferences.newInstance(), ClusterInfo.clustered());
    
    waitDeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount());
    
    JSONValue response = client.resource("/clusters/ftest/hosts/distributions")
        .queryParam("d", "*").queryParam("v", "*")
        .queryParam("rev", "previous")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUndeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, 0);
    
    JSONArray dists = client.resource("/clusters/ftest/hosts/distributions")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(dists.size(), 0);
    
    response = client.resource("/clusters/ftest/hosts/distributions/revisions/previous")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    
    dists = client.resource("/clusters/ftest/hosts/distributions")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(dists.size(), client.getHostCount());
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
    
    waitDeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, 1);
    
    JSONArray dists = client.resource("/clusters/ftest/distributions")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(dists.size(), 1);
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
      JSONValue response = client.resource("/clusters/ftest/" + partition  + "/distributions")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity(fis, MediaType.APPLICATION_OCTET_STREAM), JSONValue.class);
      assertEquals(200, response.asObject().getInt("status"));
    }
    
    waitDeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, 1);
    
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
    client.getConnector().getDeployerFacade().deployDistribution(matches[0].getAbsolutePath(), DeployPreferences.newInstance(), ClusterInfo.clustered());
    
    waitDeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount());
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral()  + "/distributions")
        .queryParam("d", "*").queryParam("v", "*")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUndeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount() - 1);
    
    JSONArray dists = client.resource("/clusters/ftest/hosts/distributions")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(dists.size(), client.getHostCount() - 1);
  }
  
  @Test
  public void testUndeployDist_partition() throws Exception {
    
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
    client.getConnector().getDeployerFacade().deployDistribution(matches[0].getAbsolutePath(), DeployPreferences.newInstance(), ClusterInfo.clustered());
    
    waitDeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount());
    
    JSONValue response = client.resource("/clusters/ftest/" + partition + "/distributions")
        .queryParam("d", "*").queryParam("v", "*")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUndeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount() - 1);
    
    JSONArray dists = client.resource("/clusters/ftest/hosts/distributions")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(dists.size(), client.getHostCount() - 1);
  }
  
  @Test
  public void testArchiveUnarchiveDist_specific_host() throws Exception {
    File[] matches = client.getConnector().getContext().getFileSystem().getBaseDir().listFiles(
        new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.endsWith("demo.zip");
          }
        }
    );
    assertEquals(1, matches.length, "Could not match");
    client.getConnector().getDeployerFacade().deployDistribution(matches[0].getAbsolutePath(), DeployPreferences.newInstance(), ClusterInfo.clustered());
    
    waitDeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount());
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/distributions")
        .queryParam("d", "*").queryParam("v", "*")
        .queryParam("rev", "previous")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUndeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount() - 1);
    
    JSONArray dists = client.resource("/clusters/ftest/hosts/distributions")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(dists.size(), client.getHostCount() - 1);
    
    response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/distributions/revisions/previous")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    
    waitDeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount());
    
    dists = client.resource("/clusters/ftest/hosts/distributions")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(dists.size(), client.getHostCount());
  }
  
  @Test
  public void testArchiveUnarchiveDist_partition() throws Exception {
    
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
    client.getConnector().getDeployerFacade().deployDistribution(matches[0].getAbsolutePath(), DeployPreferences.newInstance(), ClusterInfo.clustered());
    
    waitDeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount());
    
    JSONValue response = client.resource("/clusters/ftest/" + partition + "/distributions")
        .queryParam("d", "*").queryParam("v", "*")
        .queryParam("rev", "previous")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUndeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount() - 1);
    
    JSONArray dists = client.resource("/clusters/ftest/hosts/distributions")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(dists.size(), client.getHostCount() - 1);
    
    response = client.resource("/clusters/ftest/" + partition + "/distributions/revisions/previous")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    
    waitDeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount());
    
    dists = client.resource("/clusters/ftest/hosts/distributions")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(dists.size(), client.getHostCount());
  }
  
  // --------------------------------------------------------------------------
  // pre/post-deploy
  
  @Test
  public void testDeployDist_run_scripts_specific_host() throws Exception {
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
        .queryParam("runScripts", "true")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity(fis, MediaType.APPLICATION_OCTET_STREAM), JSONValue.class);
      assertEquals(200, response.asObject().getInt("status"));
    }
    
    waitDeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, 1);
    
    JSONArray dists = client.resource("/clusters/ftest/distributions")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(dists.size(), 1);
  }
  
  @Test
  public void testDeployDist_run_scripts_cluster() throws Exception {
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
      JSONValue response = client.resource("/clusters/ftest/hosts/distributions")
        .queryParam("runScripts", "true")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity(fis, MediaType.APPLICATION_OCTET_STREAM), JSONValue.class);
      assertEquals(200, response.asObject().getInt("status"));
    }
    
    waitDeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount());
    
    JSONArray dists = client.resource("/clusters/ftest/distributions")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(dists.size(), client.getHostCount());
  }
  
  // --------------------------------------------------------------------------
  // rollback
  
  @Test
  public void testRollback_cluster() throws Exception {
    File[] matches = client.getConnector().getContext().getFileSystem().getBaseDir().listFiles(
        new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.endsWith("demo.zip");
          }
        }
    );
    assertEquals(1, matches.length, "Could not match");
    client.getConnector().getDeployerFacade().deployDistribution(matches[0].getAbsolutePath(), DeployPreferences.newInstance().executeDeployScripts(), ClusterInfo.clustered());
    
    waitDeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount());
    
    JSONValue response = client.resource("/clusters/ftest/hosts/distributions/demo/1.0/rollback")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUndeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, 0);
    
    JSONArray dists = client.resource("/clusters/ftest/hosts/distributions")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(dists.size(), 0);
  }
  
  @Test
  public void testRollback_specific_host() throws Exception {
    File[] matches = client.getConnector().getContext().getFileSystem().getBaseDir().listFiles(
        new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.endsWith("demo.zip");
          }
        }
    );
    assertEquals(1, matches.length, "Could not match");
    client.getConnector().getDeployerFacade().deployDistribution(matches[0].getAbsolutePath(), DeployPreferences.newInstance().executeDeployScripts(), ClusterInfo.clustered());
    
    waitDeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount());
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/distributions/demo/1.0/rollback")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUndeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount() - 1);
    
    JSONArray dists = client.resource("/clusters/ftest/hosts/distributions")
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(dists.size(), client.getHostCount() - 1);
  }
  
  @Test
  public void testRollback_partition() throws Exception {
    
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
    client.getConnector().getDeployerFacade().deployDistribution(matches[0].getAbsolutePath(), DeployPreferences.newInstance().executeDeployScripts(), ClusterInfo.clustered());
    
    waitDeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount());
    
    JSONValue response = client.resource("/clusters/ftest/" +  partition + "/distributions/demo/1.0/rollback")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUndeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount() - 1);
    
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
  
  
  
  private void waitDeployed(long timeout, long pollInterval, int expectedCount)  throws InterruptedException {
    Delay delay = new Delay(timeout, TimeUnit.MILLISECONDS);
    List<Distribution> dists = new ArrayList<Distribution>();
    while (delay.isNotOver() && dists.size() < expectedCount) {
      dists.clear();
      Results<List<Distribution>> results = client.getConnector().getDeployerFacade().getDistributions(DistributionCriteria.builder().all(), ClusterInfo.clustered());
      while (results.hasNext()) {
        List<Distribution> r = results.next().getData();
        dists.addAll(r);
      }
      
      if (dists.size() >= expectedCount) {
        break;
      }
      Thread.sleep(pollInterval);
    }
   
    assertTrue(dists.size() >= expectedCount, "Expected distribution on " + expectedCount + " hosts. Got: " + dists.size());
  }
  
  private void waitUndeployed(long timeout, long pollInterval, int expectedCount)  throws InterruptedException {
    Delay delay = new Delay(timeout, TimeUnit.MILLISECONDS);
    List<Distribution> dists = new ArrayList<Distribution>();
    while (delay.isNotOver() && dists.size() > expectedCount) {
      dists.clear();
      Results<List<Distribution>> results = client.getConnector().getDeployerFacade().getDistributions(DistributionCriteria.builder().all(), ClusterInfo.clustered());
      while (results.hasNext()) {
        List<Distribution> r = results.next().getData();
        dists.addAll(r);
      }
      
      if (dists.size() <= expectedCount) {
        break;
      }
      Thread.sleep(pollInterval);
    }
   
    assertTrue(dists.size() <= expectedCount, "Expected distribution on " + expectedCount + " hosts. Got: " + dists.size());
  }
}
