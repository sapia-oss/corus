package org.sapia.corus.client.services.deployer.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Models a network, stream-based connection.
 * 
 * @author Yanick Duchesne
 */
public interface Connection {
	
	/**
	 * @return the <code>InputStream</code> of this client.
	 * @throws IOException
	 */
	public InputStream getInputStream() throws IOException;
	
	
	/**
	 * @return the <code>OutputStream</code> of this client.
	 * @throws IOException
	 */
	public OutputStream getOutputStream() throws IOException;
	
	/**
	 * Closes this connection.
	 */
	public void close();
	

}
