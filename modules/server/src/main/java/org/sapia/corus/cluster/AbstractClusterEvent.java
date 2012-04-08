package org.sapia.corus.cluster;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.services.cluster.ServerHost;
import org.sapia.ubik.net.ServerAddress;

/**
 * The abstract class for Corus clustering events.
 * 
 * @author yduchesne
 *
 */
public class AbstractClusterEvent implements Externalizable {
	
  private ServerAddress origin;
  private ServerHost    hostInfo;
  
	/**
	 * Meant for externalization
	 */
  public AbstractClusterEvent() {
  }
  
  /**
   * @param origin the {@link ServerAddress} of the node from which this event originates.
   * @param hostInfo the {@link ServerHost} of the node from which this event originates.
   */
  public AbstractClusterEvent(ServerAddress origin, ServerHost hostInfo) {
	  this.origin   = origin;
	  this.hostInfo = hostInfo;
  }
  
  /**
   * @return this instance's {@link ServerHost}, identifying the node from which
   * this event originates.
   */
  public ServerHost getHostInfo() {
	  return hostInfo;
  }
  
  /**
   * @return the {@link ServerAddress} of the node from which this event
   * originates.
   */
  public ServerAddress getOrigin() {
	  return origin;
  }  
	
	@Override
	public void readExternal(ObjectInput in) throws IOException,
	    ClassNotFoundException {
		origin   = (ServerAddress) in.readObject();
		hostInfo = (ServerHost) in.readObject();
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(origin);
		out.writeObject(hostInfo);
	}

}
