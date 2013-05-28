package org.sapia.corus.client.services.cluster;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.mcast.EventChannel;
import org.sapia.ubik.mcast.EventChannel.Role;

/**
 * Encapsulates the status in the cluster for a given Corus host.
 * 
 * @author yduchesne
 *
 */
public class ClusterStatus implements Externalizable {
	
	static final long serialVersionUID = 1L;

	private Role 				role;
	private CorusHost 	host;
	
	/** DO NOT USE: meant for externalization only.  */
	public ClusterStatus() {
  }

	public ClusterStatus(Role role, CorusHost host) {
		this.role = role;
		this.host = host;
  }
	
	/**
	 * @return the Corus instance's role.
	 */
	public Role getRole() {
	  return role;
  }
	
	/**
	 * @return the Corus host information.
	 */
	public CorusHost getHost() {
	  return host;
  }
	
	// --------------------------------------------------------------------------
	// Externalizable interface
	
	@Override
	public void readExternal(ObjectInput in) throws IOException,
	    ClassNotFoundException {
	  role = (EventChannel.Role) in.readObject();
	  host = (CorusHost) in.readObject();
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
	  out.writeObject(role);
	  out.writeObject(host);
	}
}
