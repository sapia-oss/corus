package org.sapia.corus.ftest.rest.func;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.text.StrLookup;
import org.apache.http.HttpStatus;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.Interpreter;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.Delay;
import org.sapia.corus.client.services.deployer.DeployPreferences;
import org.sapia.corus.client.services.deployer.DistributionCriteria;
import org.sapia.corus.client.services.deployer.UndeployPreferences;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.ftest.FtestClient;
import org.sapia.corus.ftest.JSONValue;
import org.sapia.corus.ftest.PartitionInfo;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Condition;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class ProcessResourcesFuncTest {
  
  private static final long DEPLOY_TIMEOUT        = 10000;
  private static final long DEPLOY_CHECK_INTERVAL = 2000;
  
 private static final int MAX_ATTEMPTS      = 20;
 private static final int INTERVAL_SECONDS  = 6;

 private FtestClient client;
  
  @BeforeSuite
  public void beforeSuite() throws Exception {
    client = FtestClient.open();
    
    File[] matches = client.getConnector().getContext().getFileSystem().getBaseDir().listFiles(
        new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return name.endsWith("demo.zip");
          }
        }
    );
    assertEquals(1, matches.length, "Could not match");
    
    // undeploy demo dist if already deployed.
    tearDown();
    client.getConnector().getDeployerFacade().undeployDistribution(DistributionCriteria.builder().all(), UndeployPreferences.newInstance(), ClusterInfo.clustered());
    
    // deploy demo dist
    client.getConnector().getDeployerFacade().deployDistribution(matches[0].getAbsolutePath(), DeployPreferences.newInstance(), ClusterInfo.clustered());
    waitDeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, client.getHostCount());
  }
  
  @AfterSuite
  public void afterSuite() throws Exception {
    // sleeping for given time to process state to be saved after an asynchronous operation
    Thread.sleep(INTERVAL_SECONDS);
    tearDown();
    client.getConnector().getDeployerFacade().undeployDistribution(DistributionCriteria.builder().all(), UndeployPreferences.newInstance(), ClusterInfo.clustered());
    waitUndeployed(DEPLOY_TIMEOUT, DEPLOY_CHECK_INTERVAL, 0);
    client.close();
  }
  
  @BeforeMethod
  public void beforeMethod() throws Exception {
    tearDown();
  }
  
  private void tearDown() {
    Interpreter interp = new Interpreter(client.getConnector());
    try {
      interp.eval("kill -d demo -v * -n noopApp -w 120 -cluster", StrLookup.noneLookup());
    } catch (Throwable err) {
      throw new IllegalStateException("Could not kill processes", err);
    }
  }
  
  // --------------------------------------------------------------------------
  // clustered
  
  @Test
  public void testExec_clustered() throws Exception {
    
    JSONValue response = client.resource("/clusters/ftest/processes/exec")
        .queryParam("d", "demo")
        .queryParam("v", "*")
        .queryParam("n", "noopApp")
        .queryParam("p", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUntilRunning(MAX_ATTEMPTS, INTERVAL_SECONDS, client.getHostCount());
  }
  
  @Test
  public void testKill_clustered() throws Exception {
    
    client.getConnector().getProcessorFacade().exec(
        ProcessCriteria.builder()
          .distribution("demo")
          .version(ArgMatchers.any())
          .name("noopApp")
          .profile("test").build(), 1, ClusterInfo.clustered());
    
    waitUntilRunning(MAX_ATTEMPTS, INTERVAL_SECONDS, client.getHostCount());
    
    JSONValue response = client.resource("/clusters/ftest/processes/kill")
        .queryParam("d", "demo")
        .queryParam("v", "*")
        .queryParam("n", "noopApp")
        .queryParam("p", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUntilTerminated(MAX_ATTEMPTS, INTERVAL_SECONDS, 0);
  }
  
  @Test
  public void testSuspendResume_clustered() throws Exception {
    
    client.getConnector().getProcessorFacade().exec(
        ProcessCriteria.builder()
          .distribution("demo")
          .version(ArgMatchers.any())
          .name("noopApp")
          .profile("test").build(), 1, ClusterInfo.clustered());
    
    waitUntilRunning(MAX_ATTEMPTS, INTERVAL_SECONDS, client.getHostCount());
    
    JSONValue response = client.resource("/clusters/ftest/processes/suspend")
        .queryParam("d", "demo")
        .queryParam("v", "*")
        .queryParam("n", "noopApp")
        .queryParam("p", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUntilStatus(MAX_ATTEMPTS, INTERVAL_SECONDS, client.getHostCount(), LifeCycleStatus.SUSPENDED);
    
    response = client.resource("/clusters/ftest/processes/resume")
        .queryParam("d", "demo")
        .queryParam("v", "*")
        .queryParam("n", "noopApp")
        .queryParam("p", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUntilStatus(MAX_ATTEMPTS, INTERVAL_SECONDS, client.getHostCount(), LifeCycleStatus.ACTIVE);
  }
  
  @Test
  public void testRestart_clustered() throws Exception {
    
    client.getConnector().getProcessorFacade().exec(
        ProcessCriteria.builder()
          .distribution("demo")
          .version(ArgMatchers.any())
          .name("noopApp")
          .profile("test").build(), 1, ClusterInfo.clustered());
    
    waitUntilRunning(MAX_ATTEMPTS, INTERVAL_SECONDS, client.getHostCount());
    
    Results<List<org.sapia.corus.client.services.processor.Process>> results = client.getConnector().getProcessorFacade().getProcesses(
        ProcessCriteria.builder().distribution("demo").name("noopApp").version(ArgMatchers.parse("*")).profile("test").build(), 
        ClusterInfo.clustered()
     );
    
    Set<String> currentCorusPids = new HashSet<>();
    while (results.hasNext()) {
      Result<List<org.sapia.corus.client.services.processor.Process>> result = results.next();
      for (org.sapia.corus.client.services.processor.Process p : result.getData()) {
        currentCorusPids.add(p.getOsPid());
      }
    }
    
    JSONValue response = client.resource("/clusters/ftest/processes/restart")
        .queryParam("d", "demo")
        .queryParam("v", "*")
        .queryParam("n", "noopApp")
        .queryParam("p", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUntilRestarted(MAX_ATTEMPTS, INTERVAL_SECONDS, client.getHostCount(), currentCorusPids);

  }
  
  @Test
  public void testClean_clustered() throws Exception {
    JSONValue response = client.resource("/clusters/ftest/processes/clean")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
  }
  
  // --------------------------------------------------------------------------
  // specific host
  
  @Test
  public void testExec_specific_host() throws Exception {
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/processes/exec")
        .queryParam("d", "demo")
        .queryParam("v", "*")
        .queryParam("n", "noopApp")
        .queryParam("p", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUntilRunning(MAX_ATTEMPTS, INTERVAL_SECONDS, 1);
    
    Thread.sleep(INTERVAL_SECONDS);
    
    JSONArray procs = client.resource("/clusters/ftest/processes")
        .queryParam("d", "demo")
        .queryParam("v", "*")
        .queryParam("n", "noopApp")
        .queryParam("p", "test")        .request()
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    
    assertEquals(procs.size(), 1);
  }
  
  @Test
  public void testExec_partition() throws Exception {
    
    PartitionInfo partition = client.createPartitionSet();
    
    JSONValue response = client.resource("/clusters/ftest/" + partition + "/processes/exec")
        .queryParam("d", "demo")
        .queryParam("v", "*")
        .queryParam("n", "noopApp")
        .queryParam("p", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON)
          .put(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUntilRunning(MAX_ATTEMPTS, INTERVAL_SECONDS, 1);
    
    Thread.sleep(INTERVAL_SECONDS);
    
    JSONArray procs = client.resource("/clusters/ftest/processes")
        .queryParam("d", "demo")
        .queryParam("v", "*")
        .queryParam("n", "noopApp")
        .queryParam("p", "test")        .request()
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    
    assertEquals(procs.size(), 1);
  }
  
  @Test 
  public void testExec_specific_host_with_diagnostic() throws Exception {
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/processes/exec")
        .queryParam("d", "demo")
        .queryParam("v", "*")
        .queryParam("n", "noopApp")
        .queryParam("p", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    
    assertEquals(200, response.asObject().getInt("status"));
    waitUntilDiagnostic(INTERVAL_SECONDS, 1);
  }
  
  @Test
  public void testKill_specific_host() throws Exception {
    
    client.getConnector().getProcessorFacade().exec(
        ProcessCriteria.builder()
          .distribution("demo")
          .version(ArgMatchers.any())
          .name("noopApp")
          .profile("test").build(), 1, ClusterInfo.clustered());
    
    waitUntilRunning(MAX_ATTEMPTS, INTERVAL_SECONDS, client.getHostCount());
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/processes/kill")
        .queryParam("d", "demo")
        .queryParam("v", "*")
        .queryParam("n", "noopApp")
        .queryParam("p", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));

    waitUntilTerminated(MAX_ATTEMPTS, INTERVAL_SECONDS, client.getHostCount() - 1);
    
    Thread.sleep(INTERVAL_SECONDS);
    
    JSONArray procs = client.resource("/clusters/ftest/processes")
        .queryParam("d", "demo")
        .queryParam("v", "*")
        .queryParam("n", "noopApp")
        .queryParam("p", "test")        .request()
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    
    assertEquals(procs.size(), 1);
    
  }
  
  @Test
  public void testKill_partition() throws Exception {
    
    PartitionInfo partition = client.createPartitionSet();
    
    client.getConnector().getProcessorFacade().exec(
        ProcessCriteria.builder()
          .distribution("demo")
          .version(ArgMatchers.any())
          .name("noopApp")
          .profile("test").build(), 1, ClusterInfo.clustered());
    
    waitUntilRunning(MAX_ATTEMPTS, INTERVAL_SECONDS, client.getHostCount());
    
    JSONValue response = client.resource("/clusters/ftest/" + partition  + "/processes/kill")
        .queryParam("d", "demo")
        .queryParam("v", "*")
        .queryParam("n", "noopApp")
        .queryParam("p", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));

    waitUntilTerminated(MAX_ATTEMPTS, INTERVAL_SECONDS, client.getHostCount() - 1);
    
    Thread.sleep(INTERVAL_SECONDS);
    
    JSONArray procs = client.resource("/clusters/ftest/processes")
        .queryParam("d", "demo")
        .queryParam("v", "*")
        .queryParam("n", "noopApp")
        .queryParam("p", "test")        .request()
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    
    assertEquals(procs.size(), 1);
    
  }
  
  @Test
  public void testSuspendResume_specific_host() throws Exception {
    client.getConnector().getProcessorFacade().exec(
        ProcessCriteria.builder()
          .distribution("demo")
          .version(ArgMatchers.any())
          .name("noopApp")
          .profile("test").build(), 1, ClusterInfo.clustered());
    
    waitUntilRunning(MAX_ATTEMPTS, INTERVAL_SECONDS, client.getHostCount());
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/processes/suspend")
        .queryParam("d", "demo")
        .queryParam("v", "*")
        .queryParam("n", "noopApp")
        .queryParam("p", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUntilStatus(MAX_ATTEMPTS, INTERVAL_SECONDS, 1, LifeCycleStatus.SUSPENDED);
    
    response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/processes/resume")
        .queryParam("d", "demo")
        .queryParam("v", "*")
        .queryParam("n", "noopApp")
        .queryParam("p", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUntilStatus(MAX_ATTEMPTS, INTERVAL_SECONDS, client.getHostCount(), LifeCycleStatus.ACTIVE);
  }
  
  @Test
  public void testSuspendResume_partition() throws Exception {
    
    PartitionInfo partition = client.createPartitionSet();
    
    client.getConnector().getProcessorFacade().exec(
        ProcessCriteria.builder()
          .distribution("demo")
          .version(ArgMatchers.any())
          .name("noopApp")
          .profile("test").build(), 1, ClusterInfo.clustered());
    
    waitUntilRunning(MAX_ATTEMPTS, INTERVAL_SECONDS, client.getHostCount());
    
    JSONValue response = client.resource("/clusters/ftest/" + partition + "/processes/suspend")
        .queryParam("d", "demo")
        .queryParam("v", "*")
        .queryParam("n", "noopApp")
        .queryParam("p", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUntilStatus(MAX_ATTEMPTS, INTERVAL_SECONDS, 1, LifeCycleStatus.SUSPENDED);
    
    response = client.resource("/clusters/ftest/" + partition + "/processes/resume")
        .queryParam("d", "demo")
        .queryParam("v", "*")
        .queryParam("n", "noopApp")
        .queryParam("p", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUntilStatus(MAX_ATTEMPTS, INTERVAL_SECONDS, client.getHostCount(), LifeCycleStatus.ACTIVE);
  }
  
  @Test
  public void testSuspendResume_specific_host_and_process() throws Exception {
    client.getConnector().getProcessorFacade().exec(
        ProcessCriteria.builder()
          .distribution("demo")
          .version(ArgMatchers.any())
          .name("noopApp")
          .profile("test").build(), 1, ClusterInfo.clustered());
    
    waitUntilRunning(MAX_ATTEMPTS, INTERVAL_SECONDS, client.getHostCount());
 
    Results<List<org.sapia.corus.client.services.processor.Process>> results = client.getConnector().getProcessorFacade().getProcesses(
        ProcessCriteria.builder().distribution("demo").name("noopApp").version(ArgMatchers.parse("*")).profile("test").build(), 
        ClusterInfo.notClustered()
     );
    
    org.sapia.corus.client.services.processor.Process p = results.next().getData().get(0);
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/processes/" + p.getProcessID() + "/suspend")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUntilStatus(MAX_ATTEMPTS, INTERVAL_SECONDS, 1, LifeCycleStatus.SUSPENDED);
    
    response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/processes/" + p.getProcessID() + "/resume")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUntilStatus(MAX_ATTEMPTS, INTERVAL_SECONDS, client.getHostCount(), LifeCycleStatus.ACTIVE);
  }
  
  @Test
  public void testRestart_specific_host() throws Exception {
    
    client.getConnector().getProcessorFacade().exec(
        ProcessCriteria.builder()
          .distribution("demo")
          .version(ArgMatchers.any())
          .name("noopApp")
          .profile("test").build(), 1, ClusterInfo.clustered());
    
    waitUntilRunning(MAX_ATTEMPTS, INTERVAL_SECONDS, client.getHostCount());
    
    Results<List<org.sapia.corus.client.services.processor.Process>> results = client.getConnector().getProcessorFacade().getProcesses(
        ProcessCriteria.builder().distribution("demo").name("noopApp").version(ArgMatchers.parse("*")).profile("test").build(), 
        ClusterInfo.notClustered()
     );
    
    org.sapia.corus.client.services.processor.Process p = results.next().getData().get(0);
    
    Set<String> currentCorusPids = Collects.arrayToSet(p.getOsPid());
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/processes/restart")
        .queryParam("d", "demo")
        .queryParam("v", "*")
        .queryParam("n", "noopApp")
        .queryParam("p", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUntilRestarted(MAX_ATTEMPTS, INTERVAL_SECONDS, client.getHostCount(), currentCorusPids);

  }
  
  @Test
  public void testRestart_partition() throws Exception {
    
    PartitionInfo partition = client.createPartitionSet();
    
    client.getConnector().getProcessorFacade().exec(
        ProcessCriteria.builder()
          .distribution("demo")
          .version(ArgMatchers.any())
          .name("noopApp")
          .profile("test").build(), 1, ClusterInfo.clustered());
    
    waitUntilRunning(MAX_ATTEMPTS, INTERVAL_SECONDS, client.getHostCount());
    
    Results<List<org.sapia.corus.client.services.processor.Process>> results = client.getConnector().getProcessorFacade().getProcesses(
        ProcessCriteria.builder().distribution("demo").name("noopApp").version(ArgMatchers.parse("*")).profile("test").build(), 
        ClusterInfo.notClustered()
     );
    
    org.sapia.corus.client.services.processor.Process p = results.next().getData().get(0);
    
    Set<String> currentCorusPids = Collects.arrayToSet(p.getOsPid());
    
    JSONValue response = client.resource("/clusters/ftest/" + partition + "/processes/restart")
        .queryParam("d", "demo")
        .queryParam("v", "*")
        .queryParam("n", "noopApp")
        .queryParam("p", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUntilRestarted(MAX_ATTEMPTS, INTERVAL_SECONDS, client.getHostCount(), currentCorusPids);

  }
  
  @Test
  public void testRestart_specific_host_and_process() throws Exception {
    
    client.getConnector().getProcessorFacade().exec(
        ProcessCriteria.builder()
          .distribution("demo")
          .version(ArgMatchers.any())
          .name("noopApp")
          .profile("test").build(), 1, ClusterInfo.clustered());
    
    waitUntilRunning(MAX_ATTEMPTS, INTERVAL_SECONDS, client.getHostCount());
    
    Results<List<org.sapia.corus.client.services.processor.Process>> results = client.getConnector().getProcessorFacade().getProcesses(
        ProcessCriteria.builder().distribution("demo").name("noopApp").version(ArgMatchers.parse("*")).profile("test").build(), 
        ClusterInfo.notClustered()
     );
    
    org.sapia.corus.client.services.processor.Process p = results.next().getData().get(0);
    
    Set<String> currentCorusPids = Collects.arrayToSet(p.getOsPid());
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/processes/" + p.getProcessID() + "/restart")
        .queryParam("d", "demo")
        .queryParam("v", "*")
        .queryParam("n", "noopApp")
        .queryParam("p", "test")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitUntilRestarted(MAX_ATTEMPTS, INTERVAL_SECONDS, client.getHostCount(), currentCorusPids);

  }
  
  @Test
  public void testClean_specific_host() throws Exception {
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/processes/clean")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
  }
  
  @Test
  public void testClean_partition() throws Exception {
    
    PartitionInfo partition = client.createPartitionSet();
    
    JSONValue response = client.resource("/clusters/ftest/" + partition + "/processes/clean")
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
  }
  
  // --------------------------------------------------------------------------
  // helpers
  
  private void waitUntilRunning(int maxAttempts, int intervalSecs, int expectedProcessCount) throws Exception {
    int attempts = 0;
    int actualProcessCount = 0;
    while (attempts < maxAttempts && actualProcessCount < expectedProcessCount) {
      JSONArray procs = client.resource("/clusters/ftest/processes")
          .queryParam("d", "demo")
          .queryParam("v", "*")
          .queryParam("n", "noopApp")
          .queryParam("p", "test")
          .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class).asArray();
      
      actualProcessCount = procs.size();
      Thread.sleep(intervalSecs * 1000);
      attempts++;
    }
    if (actualProcessCount < expectedProcessCount) {
      throw new IllegalStateException("Got " + actualProcessCount + " processes running, expected: " + expectedProcessCount);
    }
  }
  
  private void waitUntilDiagnostic(int intervalSecs, int expectedProcessCount) throws Exception {
    int status = 0;
    
    do {
      Response r  = client.resource("/clusters/ftest/diagnostic")
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .get();
      status = r.getStatus();
      System.out.println("----------------> " + r.getStatusInfo());
      System.out.println("----------------> " + r.getEntity());

      if (status != HttpStatus.SC_SERVICE_UNAVAILABLE) {
        break;
      }
      Thread.sleep(intervalSecs * 1000);
      
    } while (status == HttpStatus.SC_SERVICE_UNAVAILABLE);
    
    assertEquals(status, HttpStatus.SC_OK);
    
    JSONArray procs = client.resource("/clusters/ftest/diagnostic")
        .queryParam("d", "demo")
        .queryParam("v", "*")
        .queryParam("n", "noopApp")
        .queryParam("p", "test")
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class).asArray();
    
    if (procs.size() < expectedProcessCount) {
      throw new IllegalStateException("Got " + procs.size() + " processes running, expected: " + expectedProcessCount);
    }
  }
  
  
  private void waitUntilRestarted(int maxAttempts, int intervalSecs, int expectedProcessCount, final Set<String> currentOsPids) throws Exception {
    int attempts = 0;
    int actualProcessCount = 0;
    while (attempts < maxAttempts && actualProcessCount < expectedProcessCount) {
      JSONArray procs = client.resource("/clusters/ftest/processes")
          .queryParam("d", "demo")
          .queryParam("v", "*")
          .queryParam("n", "noopApp")
          .queryParam("p", "test")        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class).asArray(new Condition<JSONObject>() {
            @Override
            public boolean apply(JSONObject item) {
              return !currentOsPids.contains(item.getJSONObject("data").getString("pid"));
            }
          });
      
      actualProcessCount = procs.size();
      Thread.sleep(intervalSecs * 1000);
      attempts++;
    }
    if (actualProcessCount < expectedProcessCount) {
      throw new IllegalStateException("Got " + actualProcessCount + " processes running, expected: " + expectedProcessCount);
    }
  }
  
  private void waitUntilStatus(int maxAttempts, int intervalSecs, int expectedProcessCount, final LifeCycleStatus expectedStatus) throws Exception {
    int attempts = 0;
    int actualProcessCount = 0;
    while (attempts < maxAttempts && actualProcessCount < expectedProcessCount) {
      JSONArray procs = client.resource("/clusters/ftest/processes")
          .queryParam("d", "demo")
          .queryParam("v", "*")
          .queryParam("n", "noopApp")
          .queryParam("p", "test")
          .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class).asArray(new Condition<JSONObject>() {
            @Override
            public boolean apply(JSONObject item) {
              return item.getJSONObject("data").getString("status").equals(expectedStatus.name());
            }
          });
      
      actualProcessCount = procs.size();
      Thread.sleep(intervalSecs * 1000);
      attempts++;
    }
    if (actualProcessCount < expectedProcessCount) {
      throw new IllegalStateException("Got " + actualProcessCount + " processes running, expected: " + expectedProcessCount);
    }
  }
  
  private void waitUntilTerminated(int maxAttempts, int intervalSecs, int expectedProcessCount) throws Exception {
    int attempts = 0;
    int actualProcessCount = 0;
    do {
      JSONArray procs = client.resource("/clusters/ftest/processes")
          .queryParam("d", "demo")
          .queryParam("v", "*")
          .queryParam("n", "noopApp")
          .queryParam("p", "test")        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class).asArray();
      
      actualProcessCount = procs.size();
      if (actualProcessCount <= expectedProcessCount) {
        break;
      }
      Thread.sleep(intervalSecs * 1000);
      attempts++;
    } while (attempts < maxAttempts && actualProcessCount > expectedProcessCount);
    if (actualProcessCount > expectedProcessCount) {
      throw new IllegalStateException("Got " + actualProcessCount + " processes still running");
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
