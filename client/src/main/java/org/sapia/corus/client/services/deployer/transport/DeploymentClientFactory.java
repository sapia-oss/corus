package org.sapia.corus.client.services.deployer.transport;

import java.io.IOException;

import org.sapia.corus.client.services.deployer.transport.http.HttpDeploymentClient;
import org.sapia.corus.client.services.deployer.transport.mplex.MplexDeploymentClient;
import org.sapia.ubik.net.ServerAddress;
import org.sapia.ubik.net.TCPAddress;
import org.sapia.ubik.rmi.server.transport.http.HttpAddress;

/**
 * A factory class that is used to obtain <code>DeploymentClient</code> instance.
 * 
 * @author Yanick Duchesne
 */
public class DeploymentClientFactory {
	
	/**
	 * Returns a <code>DeploymentClient</code> instance, given the target address passed
	 * in.
	 * <p>
	 * IMPORTANT: this method internally calls the  <code>DeploymentClient</code>'s <code>connec()</code>
	 * method before returning it. Thus, the caller MUST NOT itself call that method.
	 * 
	 * @see DeploymentClient#connect(ServerAddress) 
	 *  
	 * @param addr a <code>ServerAddress</code>.
	 * @return a <code>DeploymentClient</code> instance.
	 * @throws IOException if a problem occurs returning the client instance.
	 */
	public static DeploymentClient newDeploymentClientFor(ServerAddress addr) throws IOException{
		DeploymentClient client;
		if(addr instanceof HttpAddress){
			client = new HttpDeploymentClient();
		}
		else if(addr instanceof TCPAddress){
			client = new MplexDeploymentClient();
		}
		else{
			throw new IllegalArgumentException("Server address type not recognized");
		}
		client.connect(addr);
		return client;
	}

}
