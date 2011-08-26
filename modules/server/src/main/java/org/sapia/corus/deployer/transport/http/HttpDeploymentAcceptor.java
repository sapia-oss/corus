package org.sapia.corus.deployer.transport.http;

import org.sapia.corus.client.services.deployer.transport.http.HttpDeploymentClient;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.deployer.transport.Deployment;
import org.sapia.corus.deployer.transport.DeploymentAcceptor;
import org.sapia.corus.deployer.transport.DeploymentConnector;
import org.sapia.ubik.rmi.server.transport.http.HttpTransportProvider;

import simple.http.ProtocolHandler;
import simple.http.Request;
import simple.http.Response;

/**
 * Implements the <code>DeploymentAcceptor</code> interface over a <code>HttpTransportProvider</code>.
 * 
 * @author Yanick Duchesne
 */
public class HttpDeploymentAcceptor implements ProtocolHandler, DeploymentAcceptor{
	
  private ServerContext         _context;
	private HttpTransportProvider _provider;
	private DeploymentConnector   _connector;
	
	public HttpDeploymentAcceptor(ServerContext context, HttpTransportProvider provider){
	  _context   = context;
		_provider  = provider;
	}
	
	/**
   * @see org.sapia.corus.deployer.transport.DeploymentAcceptor#init()
   */
  public void init() throws Exception {
  }
  
  /**
   * @see org.sapia.corus.deployer.transport.DeploymentAcceptor#start()
   */
  public void start() throws Exception {
		_provider.getServiceMapper().addService(HttpDeploymentClient.DEPLOYER_CONTEXT, this);  	
  }
  
  /**
   * @see org.sapia.corus.deployer.transport.DeploymentAcceptor#stop()
   */
  public void stop() throws Exception {
  }
  
  /**
   * @see org.sapia.corus.deployer.transport.DeploymentAcceptor#registerConnector(DeploymentConnector)
   */
  public void registerConnector(DeploymentConnector connector) {
    _connector = connector;	
  }
  
  @Override
  public void handle(Request req, Response res) {
  	_connector.connect(new Deployment(_context, new HttpConnection(req, res)));
  }

}
