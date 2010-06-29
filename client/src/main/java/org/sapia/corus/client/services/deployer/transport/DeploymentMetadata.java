package org.sapia.corus.client.services.deployer.transport;

import java.util.HashSet;
import java.util.Set;

import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.rmi.server.VmId;

/**
 * Models meta-information about a given deployment.
 * 
 * @author Yanick Duchesne
 */
public class DeploymentMetadata implements java.io.Serializable{
	
  static final long serialVersionUID = 1L;
  
	private VmId _origin = VmId.getInstance();
	
	private Set<ServerAddress> _visited = new HashSet<ServerAddress>();
	private Set<ServerAddress> _targets;
	private String _fileName;
	private long _contentLen;
	private boolean _clustered;
	
	public DeploymentMetadata(String fileName, long contentLen, Set<ServerAddress> targets, boolean clustered){
		_targets = targets;
		_fileName = fileName;
		_contentLen = contentLen;
		_clustered = clustered;
	}
	
	/**
	 * @return the file name of the deployed archive.
	 */
	public String getFileName(){
		return _fileName;
	}
	
	/**
	 * @return the length (in bytes) of the deployed archive.
	 */
	public long getContentLength(){
		return _contentLen;
	}
	
	/**
	 * @return <code>true</code> if deployment is clustered.
	 */
	public boolean isClustered(){
		return _clustered;
	}
	
	/**
	 * Returns the addresses of the servers to which the deployment is targeted.
	 * <p>
	 * If no targets were specified AND the deployment is clustered, all Corus
	 * servers will be targeted.
	 * 
	 * @return a <code>Set</code> of <code>ServerAddress</code>, or
	 * <code>null</code> if no targets were specified.
	 */
	public Set<ServerAddress> getTargets(){
		return _targets;
	}
	
	/**
	 * Returns the addresses of the servers to which the deployment has been successfully uploaded.
	 *  
	 * @return a <code>Set</code> of <code>ServerAddress</code> instances.
	 */
	public Set<ServerAddress> getVisited(){
		return _visited;
	}
	
	/**
	 * @return the <code>VmId</code> of the server from which this instance originates. 
	 * @return
	 */
	public VmId getOrigin(){
	  return _origin;	
	}
}
