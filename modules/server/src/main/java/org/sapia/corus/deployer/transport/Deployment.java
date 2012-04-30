package org.sapia.corus.deployer.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;

import org.sapia.corus.client.services.deployer.transport.Connection;
import org.sapia.corus.client.services.deployer.transport.DeployOutputStream;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.corus.core.ServerContext;
import org.sapia.ubik.serialization.SerializationStreams;

/**
 * This class models a deployment on the server-side.
 * 
 * @author Yanick Duchesne
 */
public class Deployment {
	
	static final int BUFSZ = 2048;

	private ServerContext 		 context;
  private Connection 				 conn;
	private DeploymentMetadata meta;
	
	/**
	 * @param conn the {@link Connection} that represents the network
	 * link with the client that is performing the deployment.
	 */
	public Deployment(ServerContext context, Connection conn){
    this.context = context;
		this.conn = conn;
	}
  
	public DeploymentMetadata getMetadata() throws IOException{
		if(meta == null){
			ObjectInputStream ois = SerializationStreams.createObjectInputStream(conn.getInputStream());
			try{
				meta = (DeploymentMetadata)ois.readObject();  			
			}catch(ClassNotFoundException e){
				throw new IOException("Class not found: " + e.getMessage());
			}
		}
		return meta;
	}
	
	/**
	 * @return the {@link Connection} that represents the physical
	 * link with the client that is performing the deployment.
	 */
	public Connection getConnection(){
		return conn;
	}
  
  /**
   * Closes the {@link Connection} that this instance encapsulates.
   */  
	public void close(){
	  conn.close();
	}
	
	/**
	 * Streams the deployment data to the passed in stream.
	 * <p>
	 * IMPORTANT: this method closes the given stream.
	 * 
	 * @param deployOutput an {@link OutputStream}.
	 * @throws IOException if an IO problem occurs while performing this
	 * operation.
	 */
	public void deploy(DeployOutputStream deployOutput) throws IOException{
		long length = getMetadata().getContentLength();
		InputStream is = conn.getInputStream();
		long total = 0;
		byte[] buf = new byte[BUFSZ];
		int read = 0;
		while(total < length && (read = is.read(buf, 0, BUFSZ)) > 0){
			total = total + read;
			deployOutput.write(buf, 0, read);
		}
		deployOutput.close();
		ClientCallback cb = new ClientCallback(context);
		cb.handleResult(this, deployOutput.getProgressQueue());
	}
}
