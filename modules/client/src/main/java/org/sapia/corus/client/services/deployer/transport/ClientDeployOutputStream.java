package org.sapia.corus.client.services.deployer.transport;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.exceptions.core.IORuntimeException;

/**
 * @author Yanick Duchesne
 */
public class ClientDeployOutputStream implements DeployOutputStream{
	
	private OutputStream _out;
  private AbstractDeploymentClient _client;
  private ProgressQueue _queue;
  private boolean _closed;
	
	public ClientDeployOutputStream(DeploymentMetadata meta, DeploymentClient client) throws IOException{
		try{
			_client = (AbstractDeploymentClient)client;			
		}catch(ClassCastException e){
			throw new IllegalArgumentException("Instance of " + DeploymentClient.class.getName() + " expected");
		}

		_out = _client.getOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(_out);
		oos.writeObject(meta);
		oos.flush();
	}

  /**
   * @see org.sapia.corus.client.services.deployer.transport.DeployOutputStream#close()
   */
  public void close() throws IOException {
  	if(_closed){
  		return;
  	}
		try{
			ObjectInputStream ois = new ObjectInputStream(_client.getInputStream());
			_queue = (ProgressQueue)ois.readObject();
		}catch(ClassNotFoundException e){
			throw new IOException("Could not deserialize response: " + e.getMessage());
		}  	
    _client.close();
    _closed = true;
  }

  /**
   * @see org.sapia.corus.client.services.deployer.transport.DeployOutputStream#flush()
   */
  public void flush() throws IOException {
    _out.flush();
  }

  /**
   * @see org.sapia.corus.client.services.deployer.transport.DeployOutputStream#getProgressQueue()
   */
  public ProgressQueue getProgressQueue(){
  	if(!_closed){
  		try{
    		close();
  		}catch(IOException e){
  		  throw new IORuntimeException("Could not close client", e);
  		}
  	}
		if(_queue == null){
			throw new IllegalStateException("Progress not received from server");
		}
		return _queue;
  }

  /**
   * @see org.sapia.corus.client.services.deployer.transport.DeployOutputStream#write(byte[], int, int)
   */
  public void write(byte[] b, int off, int len) throws IOException {
		_out.write(b, off, len);
  }

  /**
   * @see org.sapia.corus.client.services.deployer.transport.DeployOutputStream#write(byte[])
   */
  public void write(byte[] b) throws IOException {
    _out.write(b);
  }

  /**
   * @see org.sapia.corus.client.services.deployer.transport.DeployOutputStream#write(int)
   */
  public void write(int b) throws IOException {
 	 	_out.write(b);
  }

}
