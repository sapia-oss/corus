package org.sapia.corus.deployer.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.sapia.corus.client.services.deployer.transport.Connection;
import org.sapia.corus.client.services.deployer.transport.DeployOutputStream;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.corus.core.ServerContext;

/**
 * This class models a deployment on the server-side.
 * 
 * @author Yanick Duchesne
 */
public class Deployment {
	
	static final int BUFSZ = 2048;

	private ServerContext _context;
  private Connection _conn;
	private DeploymentMetadata _meta;
	
	/**
	 * @param conn the <code>Connection</code> that represents the network
	 * link with the client that is performing the deployment.
	 */
	public Deployment(ServerContext context, Connection conn){
    _context = context;
		_conn = conn;
	}
  
	public DeploymentMetadata getMetadata() throws IOException{
		if(_meta == null){
			ObjectInputStream ois = new ObjectInputStream(_conn.getInputStream());
			try{
				_meta = (DeploymentMetadata)ois.readObject();  			
			}catch(ClassNotFoundException e){
				throw new IOException("Class not found: " + e.getMessage());
			}
		}
		return _meta;
	}
	
	/**
	 * @return the <code>Connection</code> that represents the physical
	 * link with the client that is performing the deployment.
	 */
	public Connection getConnection(){
		return _conn;
	}
  
  /**
   * Closes the <code>Connection</code> that this instance encapsulates.
   */  
	public void close(){
	  _conn.close();
	}
	
	/**
	 * Streams the deployment data to the passed in stream.
	 * <p>
	 * IMPORTANT: this method closes the given stream.
	 * 
	 * @param deployOutput an <code>OutputStream</code>.
	 * @throws IOException if an IO problem occurs while performing this
	 * operation.
	 */
	public void deploy(DeployOutputStream deployOutput) throws IOException{
		long length = getMetadata().getContentLength();
		InputStream is = _conn.getInputStream();
		long total = 0;
		byte[] buf = new byte[BUFSZ];
		int read = 0;
		while(total < length && (read = is.read(buf, 0, BUFSZ)) > 0){
			total = total + read;
			deployOutput.write(buf, 0, read);
		}
		deployOutput.close();
		ClientCallback cb = new ClientCallback(_context);
		cb.handleResult(this, deployOutput.getProgressQueue());
	}
}
