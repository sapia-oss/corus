package org.sapia.corus.deployer.transport.http;

import java.io.IOException;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.services.deployer.transport.http.HttpDeploymentClient;

import org.sapia.corus.core.ServerContext;
import org.sapia.corus.deployer.transport.Deployment;
import org.sapia.corus.deployer.transport.DeploymentAcceptor;
import org.sapia.corus.deployer.transport.DeploymentConnector;
import org.sapia.ubik.rmi.server.transport.http.Handler;
import org.sapia.ubik.rmi.server.transport.http.HttpTransportProvider;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

/**
 * Implements the {@link DeploymentAcceptor} interface over a {@link HttpTransportProvider}.
 * 
 * @author Yanick Duchesne
 */
public class HttpDeploymentAcceptor implements Handler, DeploymentAcceptor{
	
  private Logger                log = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());
  private ServerContext         context;
	private HttpTransportProvider provider;
	private DeploymentConnector   connector;
	
	public HttpDeploymentAcceptor(ServerContext context, HttpTransportProvider provider){
	  this.context   = context;
		this.provider  = provider;
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
		provider.getRouter().addHandler(HttpDeploymentClient.DEPLOYER_CONTEXT, this);  	
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
    this.connector = connector;	
  }
  
  @Override
  public void handle(Request req, Response res) {
    try {
      connector.connect(new Deployment(context, new HttpConnection(req, res)));
    } finally {
      try {
        req.getInputStream().close();      
      } catch (IOException e) {
        log.warn("Error closing incoming deployment request stream", e);
      } 
      
      try {
        res.getOutputStream().flush();
        res.getOutputStream().close();
        res.commit();
      } catch (IOException e) {
        log.warn("Error closing incoming deployment response stream", e);
      }
    }
  }
  
  @Override
  public void shutdown() {
  }
}
