package org.sapia.corus.client.services.deployer.transport.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.sapia.corus.client.services.deployer.transport.AbstractDeploymentClient;
import org.sapia.corus.client.services.deployer.transport.DeploymentClient;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.transport.http.HttpAddress;

/**
 * Implements the {@link DeploymentClient} interface over HTTP.
 * 
 * @author Yanick Duchesne
 */
public class HttpDeploymentClient extends AbstractDeploymentClient{
 
  public static final String DEPLOYER_CONTEXT = "/corus/deployer";

	private URL 						  url;
	private HttpURLConnection conn;    

  /**
   * @see org.sapia.corus.client.services.deployer.transport.DeploymentClient#close()
   */
  public void close() {
		if(conn != null){
			try{
  			conn.getOutputStream().close();
			}catch(IOException e){
				// noop
			}
			
			try{
				conn.getInputStream().close();
			}catch(IOException e){
				// noop
			}			
		}
  }

  /**
   * @see org.sapia.corus.client.services.deployer.transport.DeploymentClient#connect(org.sapia.ubik.net.ServerAddress)
   */
  public void connect(ServerAddress addr) throws IOException {
  	try{
      url = new URL(((HttpAddress)addr).toString());
  	}catch(ClassCastException e){
  		throw new IllegalArgumentException("Instance of " + HttpAddress.class.getName() + " expected");
  	}
  }

  /**
   * @see org.sapia.corus.client.services.deployer.transport.AbstractDeploymentClient#getInputStream()
   */
  protected InputStream getInputStream() throws IOException {
    return connection().getInputStream();
  }

  /**
   * @see org.sapia.corus.client.services.deployer.transport.AbstractDeploymentClient#getOutputStream()
   */
  protected OutputStream getOutputStream() throws IOException {
		return connection().getOutputStream();
  }
  
  private HttpURLConnection connection() throws IOException{
		if(conn == null){
			conn = (HttpURLConnection)url.openConnection();
			conn.setRequestMethod("POST");
			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);
		}
  	return conn;
  }
}
