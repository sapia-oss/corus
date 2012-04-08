package org.sapia.corus.deployer.transport;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.core.ServerContext;
import org.sapia.ubik.rmi.server.transport.MarshalStreamFactory;
import org.sapia.ubik.rmi.server.transport.RmiObjectOutput;

/**
 * @author Yanick Duchesne
 */
class ClientCallback {
  
  private ServerContext context;
  
  public ClientCallback(ServerContext context) {
    this.context = context;
  }
	
	void handleResult(Deployment deployment, ProgressQueue result) throws IOException{
  	ObjectOutputStream os = MarshalStreamFactory.createOutputStream(deployment.getConnection().getOutputStream());
		((RmiObjectOutput)os).setUp(deployment.getMetadata().getOrigin(), context.getTransport().getServerAddress().getTransportType());
		os.writeObject(result);
		os.flush();
		os.close();
	}

}
