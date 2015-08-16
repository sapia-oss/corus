package org.sapia.corus.ftest;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.glassfish.jersey.client.ClientConfig;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.facade.CorusConnector;
import org.sapia.corus.client.facade.CorusConnectorBuilder;
import org.sapia.corus.client.services.security.Permission;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Collects;
import org.sapia.ubik.util.Localhost;

/**
 * Utility client-side class for executing REST functional tests.
 * 
 * @author yduchesne
 * 
 */
public class FtestClient {
  
  public static final String HEADER_APP_ID  = "X-corus-app-id";
  public static final String HEADER_APP_KEY = "X-corus-app-key";
  
  public static final  String ROLE_ADMIN = "ftest-admin";
  
  private static final int DEFAULT_PORT  = 33000;
  
  private static final String APP_KEY      = UUID.randomUUID()
        .toString().toLowerCase().replace("-", "");
  private static final String APP_ID_ADMIN = ROLE_ADMIN + "-app";
  
  private static final AtomicInteger REF_COUNT = new AtomicInteger();
  
  private static FtestClient instance;
  
  private CorusConnector connector;
  private Client         client;
  
  private FtestClient(CorusConnector connector, Client client) {
    this.connector = connector;
    this.client    = client;
  }
  
  /**
   * @return the {@link CorusConnector}.
   */
  public CorusConnector getConnector() {
    if (connector == null) {
      try {
        init();
      } catch (Exception e) {
      }
    }
    return connector;
  }
  
  /**
   * @return the application key used for functional testing.
   */
  public String getAppkey() {
    return APP_KEY;
  }
  
  /**
   * @return the <code>admin</code> application ID.
   */
  public String getAdminAppId() {
    return APP_ID_ADMIN;
  }
  
  /**
   * Invoke in order to execute a HTTP PUT.
   *
   * @param path a resource path.
   * @return a new {@link WebTarget} instance.
   * @throws IOException if an I/O error occurs.
   */
  public WebTarget resource(String path) throws IOException {
    Assertions.illegalState(client == null,"Client not initialized");
    WebTarget target = client.target(UriBuilder.fromUri(url(path)));
    return target;
  }
  
  /**
   * @param path a resource path.
   * @return the URL string for the given path.
   */
  public String url(String path) {
    Assertions.illegalState(instance == null,"Client not initialized");
    TCPAddress corusAddress = getConnector().getContext().getServerHost().getEndpoint().getServerTcpAddress();
    String url = "http://" + corusAddress.getHost() + ":" + corusAddress.getPort() + "/rest" 
        + (path.startsWith("/") ? path : "/" + path);
    return url;
  }
  
  /**
   * @return the number of hosts in the cluster.
   */
  public int getHostCount() {
    Assertions.illegalState(instance == null,"Client not initialized");
    return instance.getConnector().getContext().getOtherHosts().size() + 1;
  }

  /**
   * @return the host literal corresponding to the "current" Corus server.
   */
  public String getHostLiteral() {
    Assertions.illegalState(instance == null,"Client not initialized");
    TCPAddress addr = instance.getConnector().getContext().getServerHost().getEndpoint().getServerTcpAddress();
    return addr.getHost() + ":" + addr.getPort();
  }
  
  /**
   * @return the ID of the partition set that was created.
   * @throws IOException if an I/O problem occurs.
   */
  public PartitionInfo createPartitionSet() throws IOException {
    JSONValue response = resource("/partitionsets") 
        .queryParam("partitionSize", "1")
        .request()
          .header(FtestClient.HEADER_APP_ID, getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .put(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    
    JSONObject partitionSet = response.asObject();
    String partitionSetId = partitionSet.getString("id");
    JSONArray  partitions = partitionSet.getJSONArray("partitions");
    JSONObject partition  = partitions.getJSONObject(0);
    
    return new PartitionInfo(partitionSetId, partition.getInt("index"));
  }
  
  /**
   * @param id the ID of the partition set to delete.
   * @throws IOException if an I/O problem occurs.
   */
  public void deletePartitionSet(String id) throws IOException {
    JSONValue response = resource("/partitionsets/" + id)
        .request()
          .header(FtestClient.HEADER_APP_ID, getAdminAppId())
          .header(FtestClient.HEADER_APP_KEY, getAppkey())
          .accept(MediaType.APPLICATION_JSON) 
          .post(Entity.entity("{}", MediaType.APPLICATION_JSON), JSONValue.class);
    assertEquals(200, response.asObject().getInt("status"));
  }
  
  /**
   * @return this class' singleton.
   */
  public static FtestClient open() {
    if (instance == null) {
      try {
        init();
      } catch (Exception e) {
        throw new IllegalStateException("Could not set up connection", e);
      }
    }
    REF_COUNT.incrementAndGet();
    return instance;
  }
  
  /**
   * Shuts down this instance's resources.
   */
  public void close() {
    if (REF_COUNT.decrementAndGet() == 0) {
      instance.client.close();
    }
  }

  // --------------------------------------------------------------------------
  
  private static synchronized void init() throws Exception {
    if (instance == null) {
      CorusConnector connector = CorusConnectorBuilder.newInstance()
          .host(Localhost.getPreferredLocalAddress().getHostAddress())
          .port(DEFAULT_PORT)
          .basedir(new File("../package/target"))
          .build();
      
      connector.getSecurityManagementFacade()
        .addOrUpdateRole(ROLE_ADMIN, Collects.arrayToSet(Permission.values()), ClusterInfo.clustered());
      
      connector.getApplicationKeyManagementFacade().createApplicationKey(
          APP_ID_ADMIN, APP_KEY, ROLE_ADMIN, ClusterInfo.clustered()
      );
      
      ClientConfig conf = new ClientConfig();
      conf.register(JsonMessageBodyConverter.class);
      Client client = ClientBuilder.newClient(conf);
      instance = new FtestClient(connector, client);
    }
  }

}
