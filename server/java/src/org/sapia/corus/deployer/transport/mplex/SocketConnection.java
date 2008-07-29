package org.sapia.corus.deployer.transport.mplex;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.sapia.corus.deployer.transport.Connection;

/**
 * Implements the <code>Connection</code> interface over a socket.
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class SocketConnection implements Connection{
	
	private Socket _client;
	
	SocketConnection(Socket client){
		_client = client;
	}
	
	/**
   * @see org.sapia.corus.deployer.transport.Client#getInputStream()
   */
  public InputStream getInputStream() throws IOException {
    return _client.getInputStream();
  }
  
  /**
   * @see org.sapia.corus.deployer.transport.Client#getOutputStream()
   */
  public OutputStream getOutputStream() throws IOException {
    return _client.getOutputStream();
  }
  
  /**
   * @see org.sapia.corus.deployer.transport.Connection#close()
   */
  public void close() {
  	try {
      _client.close();
    } catch (Exception e) {
      //noop
    }
  }

}
