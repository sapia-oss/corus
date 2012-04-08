package org.sapia.corus.deployer.transport.mplex;

import java.io.UnsupportedEncodingException;

import org.apache.log.Logger;
import org.sapia.corus.client.services.deployer.transport.mplex.MplexDeploymentClient;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.deployer.transport.DeploymentAcceptor;
import org.sapia.corus.deployer.transport.DeploymentConnector;
import org.sapia.ubik.net.mplex.MultiplexSocketConnector;
import org.sapia.ubik.net.mplex.StreamSelector;
import org.sapia.ubik.rmi.server.transport.socket.MultiplexSocketHelper;

/**
 * Implements the {@link DeploymentAcceptor} over a {@link MultiplexSocketConnector}.
 * 
 * @author Yanick Duchesne
 */
public class MplexDeploymentAcceptor implements StreamSelector, DeploymentAcceptor{
	
	private MultiplexSocketConnector connector;
	private ServerContext 					 context;
	private DeploymentConnector 		 deployConn;
	private Logger              		 logger;
	private Thread              		 acceptor;
	
	public MplexDeploymentAcceptor(ServerContext context, 
	                               Logger logger){
	  this.context  = context;
		this.logger   = logger;
	}
	
	/**
   * @see org.sapia.ubik.net.mplex.StreamSelector#selectStream(byte[])
   */
  public boolean selectStream(byte[] header) {
  	try{
    	String headerStr = new String(header, 0, header.length,  "UTF-8");
  	  return headerStr.startsWith(MplexDeploymentClient.DEPLOY_STREAM_HEADER);
  	}catch(UnsupportedEncodingException e){
  		return false;
  	}
  }
  
	///////// DeploymentConnector methods /////////  
  
  /**
   * @see DeploymentAcceptor#registerConnector(DeploymentConnector)
   */
  public void registerConnector(DeploymentConnector conn) {
  	deployConn = conn;
  }
  
  ///////// Life-cycle methods /////////
  
	/**
	 * @see DeploymentAcceptor#init()
	 */
	public void init() throws Exception {
		logger.debug("Creating mplex socket connector to accept deployment connections");
		connector = MultiplexSocketHelper.createSocketConnector(this);
	}  
  
  /**
   * @see DeploymentAcceptor#start()
   */
  public void start() throws Exception{
		logger.debug("Starting mplex deployment acceptor thread");  	
		acceptor = new Thread(new AcceptorThread(connector, deployConn, context, logger));
		acceptor.setName("DeploymentAcceptor");
		acceptor.setDaemon(true);
		acceptor.start();
  }
  
  /**
   * @see DeploymentAcceptor#stop()
   */
  public void stop() throws Exception {
    connector.close();
    acceptor.interrupt();
  }
}
