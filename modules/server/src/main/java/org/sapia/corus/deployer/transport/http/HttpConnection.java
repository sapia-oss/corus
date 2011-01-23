package org.sapia.corus.deployer.transport.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.sapia.corus.client.services.deployer.transport.Connection;

import simple.http.Request;
import simple.http.Response;

/**
 * Implements the <code>Connection</code> interface over HTTP response/request
 * objects.
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class HttpConnection implements Connection{
	
	private Request  _req;
	private Response _res;

  HttpConnection(Request req, Response res){
  	_req = req;
  	_res = res;
  }
  
  /**
   * @see Connection#getInputStream()
   */
  public InputStream getInputStream() throws IOException {
    try{
      return _req.getInputStream();
    }catch(Exception e){
      throw new IOException(e);
    }
  }

  /**
   * @see Connection#getOutputStream()
   */
  public OutputStream getOutputStream() throws IOException {
    try{
      return _res.getOutputStream();
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
