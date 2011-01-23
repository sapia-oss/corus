package org.sapia.corus.deployer.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.sapia.corus.client.services.deployer.transport.Connection;

/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class TestConnection implements Connection{
	
	private InputStream _is;
	private OutputStream _os;
	
	public TestConnection(InputStream in, OutputStream out){
		_os = out;
		_is = in;
	}
	
  /**
   * @see org.sapia.corus.client.services.deployer.transport.Connection#getInputStream()
   */
  public InputStream getInputStream() throws IOException {
    return _is;
  }

  /**
   * @see org.sapia.corus.client.services.deployer.transport.Connection#getOutputStream()
   */
  public OutputStream getOutputStream() throws IOException {
    return _os;
  }
  
  /**
   * @see org.sapia.corus.client.services.deployer.transport.Connection#close()
   */
  public void close() {

  }
}
