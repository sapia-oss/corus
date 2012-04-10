package org.sapia.corus.client.services.cluster;

import java.io.Serializable;

import org.sapia.ubik.mcast.EventChannel.Role;

/**
 * Encapsulates the status in the cluster for a given Corus host.
 * 
 * @author yduchesne
 *
 */
public class ClusterStatus implements Serializable {
	
	static final long serialVersionUID = 1L;

	private Role 				role;
	private ServerHost 	host;

	public ClusterStatus(Role role, ServerHost host) {
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
	public ServerHost getHost() {
	  return host;
  }
}
