package org.sapia.corus.client.services.deployer.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.ubik.net.ServerAddress;

/**
 * A class that performs the bulk of the deployment logic. Intended to be inherited by classes that provide
 * nitty-gritty connection details but will share this class' deployment logic. 
 * 
 * @author Yanick Duchesne
 */
public abstract class AbstractDeploymentClient implements DeploymentClient{
	
	final static int BUFSZ = 2048;
	
	private DeployOutputStream _client; 	
	private OutputStream _os;
	private InputStream  _is; 
	
  /**
   * @see org.sapia.corus.client.services.deployer.transport.DeploymentClient#close()
   */
  public abstract void close();
  
  /**
   * @see org.sapia.corus.client.services.deployer.transport.DeploymentClient#connect(org.sapia.ubik.net.ServerAddress)
   */
  public abstract void connect(ServerAddress addr) throws IOException;
  
  /**
   * Returns a stream that is used for deploying to the server to which this instance is connected.
   * @param meta a <code>DeploymentMetadata</code> instance.
   * @return a <code>DeploymentOutputStream</code>
   * @throws IOException
   */
  public DeployOutputStream getDeployOutputStream(DeploymentMetadata meta) throws IOException{
  	return new ClientDeployOutputStream(meta, this);
  }
  
  /**
   * @see org.sapia.corus.deployer.transport.DeploymentClient#deploy(DeploymentMetadata, InputStream))
   */
  public ProgressQueue deploy(
    DeploymentMetadata meta,
    InputStream is)
    throws IOException {
   	
   	_client = getDeployOutputStream(meta);
		byte[] buf = new byte[BUFSZ];
		long total = 0; 
		int read = 0;
 
		while((read = is.read(buf, 0, BUFSZ)) > 0 && total < meta.getContentLength()){
			total = total + read;
			_client.write(buf, 0, read);
			_client.flush();
		}
		return _client.getProgressQueue();
  }
  
  /**
   * This method returns a stream that will be used to upload deployment data.
   * 
   * @return an <code>OutputStream</code>.
   * @throws IOException if a problem occurs acquiring the given stream.
   */
  protected abstract OutputStream getOutputStream() throws IOException;

	/**
	 * This method returns a stream that will be used to acquire the deployment result.
	 * 
	 * @return an <code>InputStream</code>.
	 * @throws IOException if a problem occurs acquiring the given stream.
	 */
  protected abstract InputStream getInputStream() throws IOException;
  
}
