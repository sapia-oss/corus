package org.sapia.corus.deployer.transport;

import org.apache.log.Logger;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.deployer.transport.http.HttpDeploymentAcceptor;
import org.sapia.corus.deployer.transport.mplex.MplexDeploymentAcceptor;
import org.sapia.ubik.rmi.server.transport.http.HttpTransportProvider;
import org.sapia.ubik.rmi.server.transport.socket.MultiplexSocketTransportProvider;

/**
 * @author Yanick Duchesne
 */
public class DeploymentProcessor {
	
	private DeploymentAcceptor  acceptor;
	private DeploymentConnector connector;
	private ServerContext 			context;
	private Logger 							logger;
	
	public DeploymentProcessor(DeploymentConnector connector, ServerContext context, Logger logger){
		this.connector = connector;		
		this.logger    = logger;
		this.context   = context;
	}
	
  public void init() throws Exception{
    if(context.getTransport().getTransportProvider() instanceof MultiplexSocketTransportProvider){
    	acceptor = new MplexDeploymentAcceptor(context, logger);
    	acceptor.registerConnector(connector);
    } else if(context.getTransport().getTransportProvider() instanceof HttpTransportProvider){
    	HttpTransportProvider provider = (HttpTransportProvider)context.getTransport().getTransportProvider();
    	acceptor = new HttpDeploymentAcceptor(context, provider);
			acceptor.registerConnector(connector);    	
    }
    else{
    	throw new IllegalStateException("Transport provider not recognized; expecting mplex or http");
    }
    acceptor.init();
  }
  
  public void start() throws Exception{
  	acceptor.start();
  }
  
  public void dispose(){
  	try{
			acceptor.stop();  		
  	}catch(Exception e){
  		// noop
  	}

  }
}
