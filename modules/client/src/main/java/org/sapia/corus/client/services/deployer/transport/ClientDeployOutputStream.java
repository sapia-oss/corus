package org.sapia.corus.client.services.deployer.transport;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.exceptions.core.IORuntimeException;
import org.sapia.ubik.serialization.SerializationStreams;

/**
 * @author Yanick Duchesne
 */
public class ClientDeployOutputStream implements DeployOutputStream{
	
	private OutputStream 						 out;
  private AbstractDeploymentClient client;
  private ProgressQueue 					 queue;
  private boolean 								 closed;
	
	public ClientDeployOutputStream(DeploymentMetadata meta, DeploymentClient client) throws IOException{
		try{
			this.client = (AbstractDeploymentClient)client;			
		}catch(ClassCastException e){
			throw new IllegalArgumentException("Instance of " + DeploymentClient.class.getName() + " expected");
		}

		out = this.client.getOutputStream();
		ObjectOutputStream oos = SerializationStreams.createObjectOutputStream(out);
		oos.writeObject(meta);
		oos.flush();
	}

  /**
   * @see org.sapia.corus.client.services.deployer.transport.DeployOutputStream#close()
   */
  public void close() throws IOException {
  	if(closed){
  		return;
  	}
		try{
			ObjectInputStream ois = SerializationStreams.createObjectInputStream(client.getInputStream());
			queue = (ProgressQueue)ois.readObject();
		}catch(ClassNotFoundException e){
			throw new IOException("Could not deserialize response: " + e.getMessage());
		}catch(IOException e){
		  e.printStackTrace();
		} 	
    client.close();
    closed = true;
  }

  /**
   * @see org.sapia.corus.client.services.deployer.transport.DeployOutputStream#flush()
   */
  public void flush() throws IOException {
    out.flush();
  }

  /**
   * @see org.sapia.corus.client.services.deployer.transport.DeployOutputStream#getProgressQueue()
   */
  public ProgressQueue getProgressQueue(){
  	if(!closed){
  		try{
    		close();
  		}catch(IOException e){
  		  throw new IORuntimeException("Could not close client", e);
  		}
  	}
		if(queue == null){
			throw new IllegalStateException("Progress not received from server");
		}
		return queue;
  }

  /**
   * @see org.sapia.corus.client.services.deployer.transport.DeployOutputStream#write(byte[], int, int)
   */
  public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
  }

  /**
   * @see org.sapia.corus.client.services.deployer.transport.DeployOutputStream#write(byte[])
   */
  public void write(byte[] b) throws IOException {
    out.write(b);
  }

  /**
   * @see org.sapia.corus.client.services.deployer.transport.DeployOutputStream#write(int)
   */
  public void write(int b) throws IOException {
 	 	out.write(b);
  }

}
