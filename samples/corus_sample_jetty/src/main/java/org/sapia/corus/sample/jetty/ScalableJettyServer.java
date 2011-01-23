package org.sapia.corus.sample.jetty;

import java.io.File;

import org.sapia.corus.sample.jetty.session.DistributedSessionManager;
import org.sapia.corus.sample.jetty.session.Remoting;

public class ScalableJettyServer {

  private static final String DEFAULT_PORT = "8080";
  
  public static void main(String[] args) throws Exception{
    String portStr = System.getProperty("corus.process.port.jetty-server", DEFAULT_PORT);
    DistributedSessionManager sessionManager = new DistributedSessionManager(Remoting.lookup());
    EmbeddedServer embedded = new EmbeddedServer(Integer.parseInt(portStr), sessionManager);
    embedded.start();
  }
}
