package org.sapia.corus.deployer.transport;

import java.io.IOException;

import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.core.ServerContext;
import org.sapia.ubik.rmi.server.transport.MarshalOutputStream;

/**
 * @author Yanick Duchesne
 */
class ClientCallback {
  
  private ServerContext context;
  
  public ClientCallback(ServerContext context) {
    this.context = context;
  }
	
	void handleResult(Deployment deployment, ProgressQueue result) throws IOException{
  	MarshalOutputStream os = new MarshalOutputStream(deployment.getConnection().getOutputStream());
		os.setUp(deployment.getMetadata().getOrigin(), context.getTransport().getServerAddress().getTransportType());
		os.writeObject(result);
		os.flush();
		os.close();
	}

}
