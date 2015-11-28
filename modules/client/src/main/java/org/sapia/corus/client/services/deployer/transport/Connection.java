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
   * @return the address of the remote host from which the deployment originates.
   */
  public String getRemoteHost();

  /**
   * @return this client's {@link InputStream}.
   * @throws IOException
   */
  public InputStream getInputStream() throws IOException;

  /**
   * @return this client's {@link OutputStream}.
   * @throws IOException
   */
  public OutputStream getOutputStream() throws IOException;

  /**
   * Closes this connection.
   */
  public void close();

}
