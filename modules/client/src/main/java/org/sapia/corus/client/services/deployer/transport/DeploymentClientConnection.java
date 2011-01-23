package org.sapia.corus.client.services.deployer.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * @author Yanick Duchesne
 */
public class DeploymentClientConnection implements Connection{
	
	private AbstractDeploymentClient _client;
	
	public DeploymentClientConnection(AbstractDeploymentClient client){
		_client = client;
	}
	
  /**
   * @see org.sapia.corus.client.services.deployer.transport.Connection#close()
   */
  public void close() {
    _client.close();
  }

  /**
   * @see org.sapia.corus.client.services.deployer.transport.Connection#getInputStream()
   */
  public InputStream getInputStream() throws IOException {
    return _client.getInputStream();
  }

  /**
   * @see org.sapia.corus.client.services.deployer.transport.Connection#getOutputStream()
   */
  public OutputStream getOutputStream() throws IOException {
    return _client.getOutputStream();
  }

}
