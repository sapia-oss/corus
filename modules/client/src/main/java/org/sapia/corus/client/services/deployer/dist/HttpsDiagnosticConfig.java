package org.sapia.corus.client.services.deployer.dist;


/**
 * Implements HTTPS-based diagnostic configuration.
 * 
 * @author yduchesne
 *
 */
public class HttpsDiagnosticConfig extends HttpDiagnosticConfig {
  
  public static final String PROTOCOL_HTTPS = "https";
  
  
  public HttpsDiagnosticConfig() {
    super(PROTOCOL_HTTPS);
  }

}
