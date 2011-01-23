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
	
	private DeploymentAcceptor _acceptor;
	private DeploymentConnector _connector;
	private ServerContext _context;
	private Logger _logger;
	
	public DeploymentProcessor(DeploymentConnector connector, ServerContext context, Logger logger){
		_logger    = logger;
		_connector = connector;
		_context = context;
	}
	
  public void init() throws Exception{
    if(_context.getTransport().getTransportProvider() instanceof MultiplexSocketTransportProvider){
    	MultiplexSocketTransportProvider provider = (MultiplexSocketTransportProvider)_context.getTransport().getTransportProvider();
    	_acceptor = new MplexDeploymentAcceptor(_context, provider, _logger);
    	_acceptor.registerConnector(_connector);
    }
    else if(_context.getTransport().getTransportProvider() instanceof HttpTransportProvider){
    	HttpTransportProvider provider = (HttpTransportProvider)_context.getTransport().getTransportProvider();
    	_acceptor = new HttpDeploymentAcceptor(_context, provider);
			_acceptor.registerConnector(_connector);    	
    }
    else{
    	throw new IllegalStateException("Transport provider not recognized; expecting mplex or http");
    }
    _acceptor.init();
  }
  
  public void start() throws Exception{
  	_acceptor.start();
  }
  
  public void dispose(){
  	try{
			_acceptor.stop();  		
  	}catch(Exception e){
  		// noop
  	}

  }
}
