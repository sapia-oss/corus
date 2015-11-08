package org.sapia.corus.cloud.platform.corus.rest;

import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;

import com.google.common.base.Preconditions;

/**
 * Wraps a Jersey {@link Client} instance.
 * 
 * @author yduchesne
 * 
 */
public class CorusRestClient {
  
  public static final String HEADER_APP_ID  = "X-corus-app-id";
  public static final String HEADER_APP_KEY = "X-corus-app-key";
  
  private String           corusAddress;
  private int              corusPort;
  private CorusCredentials credentials;
  private Client           client;
  
  /**
   * @param corusAddress the address of the Corus host to connect to.
   * @param corusPort the port of the Corus host to connect to.
   * @param creds the {@link CorusCredentials} to use for authenticating.
   */
  private CorusRestClient(String corusAddress, int corusPort, CorusCredentials creds) {
    this.corusAddress = corusAddress;
    this.corusPort    = corusPort;
    this.credentials  = creds;
    
    ClientConfig conf = new ClientConfig();
    client = ClientBuilder.newClient(conf);
  }
  
  /**
   * Invoke in order to execute a HTTP PUT.
   *
   * @param path a resource path.
   * @return a new {@link WebTarget} instance.
   * @throws IOException if an I/O error occurs.
   */
  public WebTarget resource(String path) throws IOException {
    Preconditions.checkState(client != null, "Client not initialized");
    WebTarget target = client.target(UriBuilder.fromUri(url(path)));
    target.request()
          .header(HEADER_APP_ID, credentials.getAppId())
          .header(HEADER_APP_KEY, credentials.getAppKey())
          .accept(MediaType.APPLICATION_JSON);
    
    return target;
  }
  
  /**
   * @param path a resource path.
   * @return the URL string for the given path.
   */
  public String url(String path) {
    Preconditions.checkState(client != null, "Client not initialized");
    String url = "https://" + corusAddress + ":" + corusPort + "/rest" 
        + (path.startsWith("/") ? path : "/" + path);
    return url;
  }

}
