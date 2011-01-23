package org.sapia.corus.sample.jetty;


public class BasicJettyServer {

  public static void main(String[] args) throws Exception{
    EmbeddedServer embedded = new EmbeddedServer(8080);
    embedded.start();
  }
}
