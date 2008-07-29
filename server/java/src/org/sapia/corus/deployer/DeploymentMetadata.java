package org.sapia.corus.deployer;

import java.util.HashSet;
import java.util.Set;

import org.sapia.ubik.rmi.server.VmId;

/**
 * Models meta-information about a given deployment.
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class DeploymentMetadata implements java.io.Serializable{
	
	private VmId _origin = VmId.getInstance();
	
	private Set _visited = new HashSet();
	private Set _targets;
	private String _fileName;
	private long _contentLen;
	private boolean _clustered;
	
	public DeploymentMetadata(String fileName, long contentLen, Set targets, boolean clustered){
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
	public Set getTargets(){
		return _targets;
	}
	
	/**
	 * Returns the addresses of the servers to which the deployment has been successfully uploaded.
	 *  
	 * @return a <code>Set</code> of <code>ServerAddress</code> instances.
	 */
	public Set getVisited(){
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
