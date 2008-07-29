package org.sapia.corus.deployer.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.sapia.corus.deployer.DeployOutputStream;
import org.sapia.corus.deployer.DeploymentMetadata;

/**
 * This class models a deployment on the server-side.
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Deployment {
	
	static final int BUFSZ = 2048;
	
	private DeploymentMetadata _meta;
	private Connection _conn;
	
	/**
	 * @param conn the <code>Connection</code> that represents the network
	 * link with the client that is performing the deployment.
	 */
	public Deployment(Connection conn){
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
		ClientCallback cb = new ClientCallback();
		cb.handleResult(this, deployOutput.getProgressQueue());
	}
}
