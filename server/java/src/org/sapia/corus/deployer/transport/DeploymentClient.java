package org.sapia.corus.deployer.transport;

import java.io.IOException;
import java.io.InputStream;

import org.sapia.corus.deployer.DeploymentMetadata;
import org.sapia.corus.util.ProgressQueue;
import org.sapia.ubik.net.ServerAddress;

/**
 * This interface specifies the behavior of client-side deployment.
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface DeploymentClient {

	/**
	 * Connects to the Corus server corresponding to the given address.
	 *  
	 * @param addr the <code>ServerAddress</code> of the server.
	 * @throws IOException if no connection could be made.
	 */
	public void connect(ServerAddress addr) throws IOException;
		
  /**
   * Performs a deployment.
   * 
   * @param meta a <code>DeploymentMetadata</code> holding deployment
   * information used by this instance.
   * @param is the stream of data to deploy.
   * @throws IOException if a problem occurs during deployment.
   */
	public ProgressQueue deploy(DeploymentMetadata meta,
										          InputStream is) throws IOException;
										 
	/**
	 * Releases all system resources that this instance holds.
	 */									 
	public void close();

}
