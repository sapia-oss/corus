package org.sapia.corus.client.services.deployer.transport;

import java.io.IOException;
import java.io.InputStream;

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
	
	private DeployOutputStream client; 	
	
  /**
   * @see DeploymentClient#close()
   */
  public abstract void close();
  
  /**
   * @see DeploymentClient#connect(ServerAddress)
   */
  public abstract void connect(ServerAddress addr) throws IOException;
  
  /**
   * Returns a stream that is used for deploying to the server to which this instance is connected.
   * @param meta a {@link DeploymentMetadata} instance.
   * @return a {@link DeployOutputStream}
   * @throws IOException
   */
  public DeployOutputStream getDeployOutputStream(DeploymentMetadata meta) throws IOException{
  	return new ClientDeployOutputStream(meta, this);
  }
  
  /**
   * @see DeploymentClient#deploy(DeploymentMetadata, InputStream)
   */
  public ProgressQueue deploy(
    DeploymentMetadata meta,
    InputStream is)
    throws IOException {
   	
   	client = getDeployOutputStream(meta);
		byte[] buf = new byte[BUFSZ];
		long total = 0; 
		int read = 0;
 
		while((read = is.read(buf, 0, BUFSZ)) > 0 && total < meta.getContentLength()){
			total = total + read;
			client.write(buf, 0, read);
			client.flush();
		}
		return client.getProgressQueue();
  }
  
}
