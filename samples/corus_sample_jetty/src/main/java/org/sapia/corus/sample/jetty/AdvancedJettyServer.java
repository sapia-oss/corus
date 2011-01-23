package org.sapia.corus.sample.jetty;


public class AdvancedJettyServer {

  private static final String DEFAULT_PORT = "8080";
  
  public static void main(String[] args) throws Exception{
    String portStr = System.getProperty("corus.process.port.jetty-server", DEFAULT_PORT);
    EmbeddedServer embedded = new EmbeddedServer(Integer.parseInt(portStr));
    embedded.start();
  }
}
