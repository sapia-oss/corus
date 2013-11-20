package org.sapia.corus.examples;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.sapia.ubik.util.Props;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

public class HttpServer implements Container {

  private static final String HTTP_SERVER_PORT_KEY = "server.port";

  public void handle(Request request, Response response) {
    try {
      PrintStream body = response.getPrintStream();
      long time = System.currentTimeMillis();

      response.setValue("Content-Type", "text/plain");
      response.setDate("Date", time);
      response.setDate("Last-Modified", time);

      body.println("Welcome");
      body.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] list) throws Exception {
    Container container = new HttpServer();
    Server server = new ContainerServer(container);
    Connection connection = new SocketConnection(server);
    SocketAddress address = new InetSocketAddress(Props.getSystemProperties().getIntProperty(HTTP_SERVER_PORT_KEY));
    connection.connect(address);
  }

}
