package org.sapia.corus.deployer.transport.mplex;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.sapia.corus.client.services.deployer.transport.Connection;

/**
 * Implements the {@link Connection} interface over a socket.
 * 
 * @author Yanick Duchesne
 *
 */
public class SocketConnection implements Connection{
	
	private Socket client;
	
	SocketConnection(Socket client){
		this.client = client;
	}
	
	/**
   * @see Connection#getInputStream()
   */
  public InputStream getInputStream() throws IOException {
    return client.getInputStream();
  }
  
  /**
   * @see Connection#getOutputStream()
   */
  public OutputStream getOutputStream() throws IOException {
    return client.getOutputStream();
  }
  
  /**
   * @see Connection#close()
   */
  public void close() {
  	try {
      client.close();
    } catch (Exception e) {
      //noop
    }
  }

}
