package org.sapia.corus.deployer.transport.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.sapia.corus.client.services.deployer.transport.Connection;

import simple.http.Request;
import simple.http.Response;

/**
 * Implements the {@link Connection} interface over HTTP response/request
 * objects.
 * 
 * @author Yanick Duchesne
 */
public class HttpConnection implements Connection {
	
	private Request  req;
	private Response res;

  HttpConnection(Request req, Response res){
  	this.req = req;
  	this.res = res;
  }
  
  /**
   * @see Connection#getInputStream()
   */
  public InputStream getInputStream() throws IOException {
    try{
      return req.getInputStream();
    }catch(Exception e){
      throw new IOException(e);
    }
  }

  /**
   * @see Connection#getOutputStream()
   */
  public OutputStream getOutputStream() throws IOException {
    try{
      return res.getOutputStream();
    }catch(Exception e){
      throw new IOException(e);
    }

  }
  
  /**
   * @see Connection#close()
   */
  public void close() {
  }


}
