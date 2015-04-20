package org.sapia.corus.maven;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;

import org.apache.maven.plugin.MojoExecutionException;
import org.junit.Before;
import org.junit.Test;

public class WaitForProcessMojoTest {

  private WaitForProcessMojo mojo;

  @Before
  public void setUp() throws Exception {
    mojo = new WaitForProcessMojo();
  }

  protected ServerSocket doStartServerSocket(boolean isSuccessResponse) throws Exception {
    ServerSocket server = new ServerSocket();
    server.bind(server.getLocalSocketAddress());
    SimpleHttpServer httpServer = new SimpleHttpServer(server, isSuccessResponse);
    Thread t = new Thread(httpServer);
    t.setDaemon(true);
    t.start();

    return server;
  }

  @Test(expected = MojoExecutionException.class)
  public void test_noServer_withMaxRetry() throws Exception {
    mojo.setUrl("http://localhost:55555/nowhere");
    mojo.setMaxRetry(3);
    mojo.setTimeout(2000);
    mojo.doExecute();
  }

  @Test
  public void test_withServer_successResponse() throws Exception {
    ServerSocket socketServer = doStartServerSocket(true);

    mojo.setUrl("http://localhost:" + socketServer.getLocalPort() + "/nowhere");
    mojo.setMaxRetry(1);
    mojo.setTimeout(2000);
    mojo.doExecute();
  }

  @Test(expected = MojoExecutionException.class)
  public void test_withServer_failureResponse() throws Exception {
    ServerSocket socketServer = doStartServerSocket(false);

    mojo.setUrl("http://localhost:" + socketServer.getLocalPort() + "/nowhere");
    mojo.setMaxRetry(1);
    mojo.setTimeout(2000);
    mojo.doExecute();
  }

  public static class SimpleHttpServer implements Runnable {

    private ServerSocket server;
    private boolean isSuccessResponse;

    public SimpleHttpServer(ServerSocket server, boolean isSuccessResponse) {
      this.server = server;
      this.isSuccessResponse = isSuccessResponse;
    }

    @Override
    public void run() {
      try {
        Socket client = server.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        StringBuilder request = new StringBuilder();
        Readers.transfertTo(reader, request);

        System.err.println(request);

        StringBuilder response = new StringBuilder();

        if (isSuccessResponse) {
          String body = "<HTML><HEAD><TITLE>Test</TITLE></HEAD><BODY>200 Ok</BODY></HTML>";
          response.append("HTTP/1.0 200 OK\r\n")
              .append("Content-type: text/html\r\n")
              .append("Content-Length: ").append(body.length()).append("\r\n")
              .append("\r\n")
              .append(body);
        } else {
          String body = "<HTML><HEAD><TITLE>Test</TITLE></HEAD><BODY>501 Not Implemented</BODY></HTML>";
          response.append("HTTP/1.0 501 Not Implemented\r\n")
              .append("Content-type: text/html\r\n")
              .append("Content-Length: ").append(body.length()).append("\r\n")
              .append("\r\n")
              .append(body);
        }

        client.getOutputStream().write(response.toString().getBytes(Charset.forName("UTF-8")));
        client.getOutputStream().close();
        client.close();

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
