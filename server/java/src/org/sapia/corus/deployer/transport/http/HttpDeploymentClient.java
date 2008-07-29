package org.sapia.corus.deployer.transport.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.sapia.corus.deployer.transport.AbstractDeploymentClient;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.transport.http.HttpAddress;

/**
 * Implements the <code>DeploymentClient</code> interface over HTTP.
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class HttpDeploymentClient extends AbstractDeploymentClient{
 
	private URL _url;
	private HttpURLConnection _conn;    

  /**
   * @see org.sapia.corus.deployer.transport.DeploymentClient#close()
   */
  public void close() {
		if(_conn != null){
			try{
  			_conn.getOutputStream().close();
			}catch(IOException e){
				// noop
			}
			
			try{
				_conn.getInputStream().close();
			}catch(IOException e){
				// noop
			}			
		}
  }

  /**
   * @see org.sapia.corus.deployer.transport.DeploymentClient#connect(org.sapia.ubik.net.ServerAddress)
   */
  public void connect(ServerAddress addr) throws IOException {
  	try{
      _url = new URL(((HttpAddress)addr).toString());
  	}catch(ClassCastException e){
  		throw new IllegalArgumentException("Instance of " + HttpAddress.class.getName() + " expected");
  	}
  }

  /**
   * @see org.sapia.corus.deployer.transport.AbstractDeploymentClient#getInputStream()
   */
  protected InputStream getInputStream() throws IOException {
    return connection().getInputStream();
  }

  /**
   * @see org.sapia.corus.deployer.transport.AbstractDeploymentClient#getOutputStream()
   */
  protected OutputStream getOutputStream() throws IOException {
		return connection().getOutputStream();
  }
  
  private HttpURLConnection connection() throws IOException{
		if(_conn == null){
			_conn = (HttpURLConnection)_url.openConnection();
			_conn.setRequestMethod("POST");
			_conn.setUseCaches(false);
			_conn.setDoInput(true);
			_conn.setDoOutput(true);
		}
  	return _conn;
  }
}
