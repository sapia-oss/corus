package org.sapia.corus.deployer.transport.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.sapia.corus.client.services.deployer.transport.Connection;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

/**
 * Implements the {@link Connection} interface over HTTP response/request
 * objects.
 * 
 * @author Yanick Duchesne
 */
public class HttpConnection implements Connection {

  private Request req;
  private Response res;
  private OutputStream out;
  private InputStream is;

  public HttpConnection(Request req, Response res) {
    this.req = req;
    this.res = res;
  }
  
  @Override
  public String getRemoteHost() {
    return req.getClientAddress().getHostString();
  }

  /**
   * @see Connection#getInputStream()
   */
  public InputStream getInputStream() throws IOException {
    if (is == null) {
      is = req.getInputStream();
    }
    return is;
  }

  /**
   * @see Connection#getOutputStream()
   */
  public OutputStream getOutputStream() throws IOException {
    if (out == null) {
      out = res.getOutputStream();
    }
    return out;
  }

  /**
   * @see Connection#close()
   */
  public void close() {
  }

}
