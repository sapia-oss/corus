package org.sapia.corus.ftest.rest.func.docker;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.common.Delay;
import org.sapia.corus.client.common.FilePath;
import org.sapia.corus.client.common.IDGenerator;
import org.sapia.corus.client.common.IOUtil;
import org.sapia.corus.client.common.ProgressMsg;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.services.docker.DockerClientException;
import org.sapia.corus.client.services.docker.DockerImage;
import org.sapia.corus.ftest.FtestClient;
import org.sapia.corus.ftest.JSONValue;
import org.sapia.corus.ftest.PartitionInfo;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import net.sf.json.JSONArray;

public class DockerResourcesFuncTest {

  static final long CHECK_TIMEOUT  = 10000;
  static final long CHECK_INTERVAL = 2000;
  static final int  BUFSZ          = 8092;
  
  static final String MEMCACHED         = "mini/memcached:latest";
  static final String MEMCACHED_ENCODED = "[mini/memcached:latest]";
  
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
    consumeProgress(client.getConnector().getDockerManagementFacade().removeImages(ArgMatchers.parse(MEMCACHED), ClusterInfo.clustered()), false);
  }
  
  // --------------------------------------------------------------------------
  // cluster

  
  @Test
  public void testLoadImage_clustered() throws Exception {
    
    consumeProgress(client.getConnector().getDockerManagementFacade().pullImage(
        MEMCACHED, ClusterInfo.fromLiteralForm(client.getHostLiteral())), false
    );
    
    File tmpFile = FilePath.forJvmTempDir().setRelativeFile("testGetImagePayload-" + IDGenerator.makeBase62Id(5)).createFile();
    tmpFile.deleteOnExit();
    try (FileOutputStream fis = new FileOutputStream(tmpFile)) {
      try (InputStream imagePayload = client.getConnector().getDockerManagementFacade().getImagePayload(MEMCACHED)) {
        IOUtil.transfer(imagePayload, fis, BUFSZ);
      }
    }
    
    consumeProgress(client.getConnector().getDockerManagementFacade().removeImages(
        ArgMatchers.parse(MEMCACHED), ClusterInfo.fromLiteralForm(client.getHostLiteral())), false
    );
    
    try (InputStream imagePayload = new FileInputStream(tmpFile)) {
      JSONValue response = client.resource("/clusters/ftest/docker/images/" + MEMCACHED_ENCODED)
          .request()
            .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
            .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
            .accept(MediaType.APPLICATION_JSON) 
            .put(Entity.entity(imagePayload, MediaType.APPLICATION_OCTET_STREAM), JSONValue.class);
        assertEquals(200, response.asObject().getInt("status"));      
    }
    
    waitPulled(CHECK_TIMEOUT, CHECK_INTERVAL, client.getHostCount());
    
    JSONArray images = client.resource("/clusters/ftest/docker/images")
        .queryParam("n", MEMCACHED)
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(client.getHostCount(), images.size());
  }
  
  @Test
  public void testPullImage_clustered() throws Exception {
    
    JSONValue response = client.resource("/clusters/ftest/docker/images/" + MEMCACHED_ENCODED)
      .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON) 
        .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
 
    waitPulled(CHECK_TIMEOUT, CHECK_INTERVAL, client.getHostCount());
    
    JSONArray images = client.resource("/clusters/ftest/docker/images")
        .queryParam("n", MEMCACHED)
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(client.getHostCount(), images.size());
  }
  
  @Test
  public void testPullImage_clustered_rippled() throws Exception {
    JSONValue response = client.resource("/clusters/ftest/docker/images/" + MEMCACHED_ENCODED)
      .queryParam("minHosts", "1")
      .queryParam("batchSize", "1")
      .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON) 
        .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(response.asObject().getInt("status"), 200);
 
    waitPulled(CHECK_TIMEOUT, CHECK_INTERVAL, client.getHostCount());
  
    JSONArray images = client.resource("/clusters/ftest/docker/images")
      .queryParam("n", MEMCACHED)
      .request()
        .accept(MediaType.APPLICATION_JSON)
        .get(JSONValue.class)
        .asArray();
    assertEquals(client.getHostCount(), images.size());
  }
  
  
  @Test
  public void testRemoveImage_clustered() throws Exception {
    
    consumeProgress(client.getConnector().getDockerManagementFacade().pullImage(MEMCACHED, ClusterInfo.clustered()), true);
    
    JSONValue response = client.resource("/clusters/ftest/docker/images")
        .queryParam("n", MEMCACHED_ENCODED)
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitRemoved(CHECK_TIMEOUT, CHECK_INTERVAL, 0);
    
    JSONArray images = client.resource("/clusters/ftest/docker/images")
        .queryParam("n", MEMCACHED)
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(client.getHostCount(), images.size());
  }
  
  // --------------------------------------------------------------------------
  // specific host
  
  @Test
  public void testLoadImage_specific_host() throws Exception {
    
    consumeProgress(client.getConnector().getDockerManagementFacade().pullImage(
        MEMCACHED, ClusterInfo.fromLiteralForm(client.getHostLiteral())), false
    );
    
    File tmpFile = FilePath.forJvmTempDir().setRelativeFile("testGetImagePayload-" + IDGenerator.makeBase62Id(5)).createFile();
    tmpFile.deleteOnExit();
    try (FileOutputStream fis = new FileOutputStream(tmpFile)) {
      try (InputStream imagePayload = client.getConnector().getDockerManagementFacade().getImagePayload(MEMCACHED)) {
        IOUtil.transfer(imagePayload, fis, BUFSZ);
      }
    }
    
    consumeProgress(client.getConnector().getDockerManagementFacade().removeImages(
        ArgMatchers.parse(MEMCACHED), ClusterInfo.fromLiteralForm(client.getHostLiteral())), false
    );
    
    try (InputStream imagePayload = new FileInputStream(tmpFile)) {
      JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral() + "/docker/images/" + MEMCACHED_ENCODED)
          .request()
            .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
            .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
            .accept(MediaType.APPLICATION_JSON) 
            .put(Entity.entity(imagePayload, MediaType.APPLICATION_OCTET_STREAM), JSONValue.class);
        assertEquals(200, response.asObject().getInt("status"));      
    }
    
    waitPulled(CHECK_TIMEOUT, CHECK_INTERVAL, 1);
    
    JSONArray images = client.resource("/clusters/ftest/docker/images")
        .queryParam("n", MEMCACHED)
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(images.size(), 1);
  }
  

  @Test
  public void testPullImage_specific_host() throws Exception {
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral()  + "/docker/images/" + MEMCACHED_ENCODED)
      .request()
        .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
        .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
        .accept(MediaType.APPLICATION_JSON) 
        .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitPulled(CHECK_TIMEOUT, CHECK_INTERVAL, 1);
    
    JSONArray images = client.resource("/clusters/ftest/docker/images")
        .queryParam("n", MEMCACHED)
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(images.size(), 1);
  }
  
  @Test
  public void testRemoveImage_specific_host() throws Exception {
    
    consumeProgress(client.getConnector().getDockerManagementFacade().pullImage(MEMCACHED, ClusterInfo.clustered()), true);
    
    JSONValue response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral()  + "/docker/images")
        .queryParam("n", MEMCACHED)
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .delete(JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
    
    waitRemoved(CHECK_TIMEOUT, CHECK_INTERVAL, client.getHostCount() - 1);
    
    JSONArray dists = client.resource("/clusters/ftest/docker/images")
        .queryParam("n", MEMCACHED)
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(dists.size(), client.getHostCount() - 1);
  }
  
  @Test
  public void testGetImagePayload() throws Exception {
    ClusterInfo clustered = ClusterInfo.fromLiteralForm(client.getHostLiteral());
    consumeProgress(client.getConnector().getDockerManagementFacade().pullImage(MEMCACHED, clustered), true);

    Response  response = client.resource("/clusters/ftest/hosts/" + client.getHostLiteral()  + "/docker/images/[mini/memcached:latest]/payload")
        .queryParam("n", MEMCACHED)
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_OCTET_STREAM) 
          .get();
  
    assertEquals(200, response.getStatus());
    
    File tmpFile = FilePath.forJvmTempDir().setRelativeFile("testGetImagePayload-" + IDGenerator.makeBase62Id(5)).createFile();
    tmpFile.deleteOnExit();
    try (FileOutputStream fis = new FileOutputStream(tmpFile)) {
      try (InputStream imagePayload = (InputStream) response.getEntity()) {
        IOUtil.transfer(imagePayload, fis, BUFSZ);
      }
    }
    
    assertTrue(tmpFile.exists());
    assertTrue(tmpFile.length() > 0);
    
  }
  
  // --------------------------------------------------------------------------
  // partition
  
  
  @Test
  public void testLoadImage_partition() throws Exception {
    
    PartitionInfo partition = client.createPartitionSet();
    
    consumeProgress(client.getConnector().getDockerManagementFacade().pullImage(
        MEMCACHED, ClusterInfo.fromLiteralForm(client.getHostLiteral())), false
    );
    
    File tmpFile = FilePath.forJvmTempDir().setRelativeFile("testGetImagePayload-" + IDGenerator.makeBase62Id(5)).createFile();
    tmpFile.deleteOnExit();
    try (FileOutputStream fis = new FileOutputStream(tmpFile)) {
      try (InputStream imagePayload = client.getConnector().getDockerManagementFacade().getImagePayload(MEMCACHED)) {
        IOUtil.transfer(imagePayload, fis, BUFSZ);
      }
    }
    
    consumeProgress(client.getConnector().getDockerManagementFacade().removeImages(
        ArgMatchers.parse(MEMCACHED), ClusterInfo.fromLiteralForm(client.getHostLiteral())), false
    );
    
    try (InputStream imagePayload = new FileInputStream(tmpFile)) {
      JSONValue response = client.resource("/clusters/ftest/" + partition + "/docker/images/" + MEMCACHED_ENCODED)
          .request()
            .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
            .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
            .accept(MediaType.APPLICATION_JSON) 
            .put(Entity.entity(imagePayload, MediaType.APPLICATION_OCTET_STREAM), JSONValue.class);
        assertEquals(200, response.asObject().getInt("status"));      
    }
    
    waitPulled(CHECK_TIMEOUT, CHECK_INTERVAL, 1);
    
    JSONArray images = client.resource("/clusters/ftest/docker/images")
        .queryParam("f", MEMCACHED)
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    assertEquals(images.size(), 1);
  }
  
  @Test
  public void testPullImage_partition() throws Exception {
    
    PartitionInfo partition = client.createPartitionSet();

    JSONValue response = client.resource("/clusters/ftest/" + partition + "/docker/images/" + MEMCACHED_ENCODED)
        .request()
          .header(FtestClient.HEADER_APP_ID, client.getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, client.getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
 
    waitPulled(CHECK_TIMEOUT, CHECK_INTERVAL, 1);
    
    JSONArray images = client.resource("/clusters/ftest/docker/images")
        .queryParam("f", MEMCACHED)
        .request()
          .accept(MediaType.APPLICATION_JSON)
          .get(JSONValue.class)
          .asArray();
    
    assertEquals(images.size(), 1);

  }
  
  // --------------------------------------------------------------------------
  // Restricted
  
  private void waitRemoved(long timeout, long pollInterval, int expectedCount) throws DockerClientException, InterruptedException {
    Delay delay = new Delay(timeout, TimeUnit.MILLISECONDS);
    List<DockerImage> images = new ArrayList<DockerImage>();
    while (delay.isNotOver() && images.size() > expectedCount) {
      images.clear();
      Results<List<DockerImage>> results = client.getConnector().getDockerManagementFacade().getImages(ArgMatchers.parse(MEMCACHED), ClusterInfo.clustered());
      while (results.hasNext()) {
        List<DockerImage> r = results.next().getData();
        images.addAll(r);
      }
      
      if (images.size() <= expectedCount) {
        break;
      }
      Thread.sleep(pollInterval);
    }
   
    assertTrue(images.size() <= expectedCount, "Expected image on " + expectedCount + " hosts. Got: " + images.size());
  }
  
  
  private void waitPulled(long timeout, long pollInterval, int expectedCount) throws DockerClientException, InterruptedException {
    Delay delay = new Delay(timeout, TimeUnit.MILLISECONDS);
    List<DockerImage> images = new ArrayList<DockerImage>();
    while (delay.isNotOver() && images.size() < expectedCount) {
      images.clear();
      Results<List<DockerImage>> results = client.getConnector().getDockerManagementFacade().getImages(ArgMatchers.parse(MEMCACHED), ClusterInfo.clustered());
      while (results.hasNext()) {
        List<DockerImage> r = results.next().getData();
        images.addAll(r);
      }
      
      if (images.size() >= expectedCount) {
        break;
      }
      Thread.sleep(pollInterval);
    }
   
    assertTrue(images.size() >= expectedCount, "Expected image on " + expectedCount + " hosts. Got: " + images.size());
  }
  
  private void consumeProgress(ProgressQueue queue, boolean lenient) {
    while (queue.hasNext()) {
      List<ProgressMsg> msgs = queue.next();
      for (ProgressMsg m : msgs) {
        if (!lenient && m.isError()) {
          if (m.isThrowable()) {
            throw new IllegalStateException("Error occurred", m.getThrowable());
          } else {
            throw new IllegalStateException("Error occurred: " + m.getMessage().toString());
          }
        }
      }
    }
  }
}
