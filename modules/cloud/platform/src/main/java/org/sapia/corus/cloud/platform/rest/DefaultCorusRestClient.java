package org.sapia.corus.cloud.platform.rest;

import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientConfig;
import org.sapia.corus.cloud.platform.domain.CorusAddress;

import com.google.common.base.Preconditions;

/**
 * Wraps a Jersey {@link Client} instance.
 * 
 * @author yduchesne
 * 
 */
public class DefaultCorusRestClient implements CorusRestClient {
  
  private CorusAddress     corusAddress;
  private CorusCredentials credentials;
  private Client           client;
  
  /**
   * @param corusAddress the address of the Corus host to connect to.
   * @param corusPort the port of the Corus host to connect to.
   * @param creds the {@link CorusCredentials} to use for authenticating.
   */
  public DefaultCorusRestClient(CorusAddress address, CorusCredentials creds) {
    this.corusAddress = address;
    this.credentials  = creds;
    
    ClientConfig conf = new ClientConfig();
    client = ClientBuilder.newClient(conf);
  }
  
  @Override
  public WebTarget resource(String path) throws IOException {
    Preconditions.checkState(client != null, "Client not initialized");
    WebTarget target = client.target(UriBuilder.fromUri(url(path)));
    target.request()
          .header(HEADER_APP_ID, credentials.getAppId())
          .header(HEADER_APP_KEY, credentials.getAppKey())
          .accept(MediaType.APPLICATION_JSON);
    
    return target;
  }
  
  @Override
  public String url(String path) {
    Preconditions.checkState(client != null, "Client not initialized");
    String url = corusAddress.asHttpsUrl() +  "/rest" 
        + (path.startsWith("/") ? path : "/" + path);
    return url;
  }
  
  @Override
  public void close() {
    client.close();
  }

}
