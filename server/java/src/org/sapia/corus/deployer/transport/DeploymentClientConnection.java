package org.sapia.corus.deployer.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class DeploymentClientConnection implements Connection{
	
	private AbstractDeploymentClient _client;
	
	public DeploymentClientConnection(AbstractDeploymentClient client){
		_client = client;
	}
	
  /**
   * @see org.sapia.corus.deployer.transport.Connection#close()
   */
  public void close() {
    _client.close();
  }

  /**
   * @see org.sapia.corus.deployer.transport.Connection#getInputStream()
   */
  public InputStream getInputStream() throws IOException {
    return _client.getInputStream();
  }

  /**
   * @see org.sapia.corus.deployer.transport.Connection#getOutputStream()
   */
  public OutputStream getOutputStream() throws IOException {
    return _client.getOutputStream();
  }

}
