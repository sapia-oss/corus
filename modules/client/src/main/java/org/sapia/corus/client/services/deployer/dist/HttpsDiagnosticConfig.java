package org.sapia.corus.client.services.deployer.dist;

import org.sapia.util.xml.confix.ConfigurationException;


/**
 * Implements HTTPS-based diagnostic configuration.
 * 
 * @author yduchesne
 *
 */
public class HttpsDiagnosticConfig extends HttpDiagnosticConfig {
  
  public static final String ELEMENT_NAME = "https-diagnostic";
  
  public static final String PROTOCOL_HTTPS = "https";
  
  
  public HttpsDiagnosticConfig() {
    super(PROTOCOL_HTTPS);
  }

  @Override
  public Object onCreate() throws ConfigurationException {
    doValidate(ELEMENT_NAME);
    return this;
  }
}
