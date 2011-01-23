package org.sapia.corus.deployer.transport.mplex;

import java.io.UnsupportedEncodingException;

import org.apache.log.Logger;
import org.sapia.corus.client.services.deployer.transport.mplex.MplexDeploymentClient;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.deployer.transport.DeploymentAcceptor;
import org.sapia.corus.deployer.transport.DeploymentConnector;
import org.sapia.ubik.net.mplex.MultiplexSocketConnector;
import org.sapia.ubik.net.mplex.StreamSelector;
import org.sapia.ubik.rmi.server.transport.socket.MultiplexSocketTransportProvider;

/**
 * Implements the <code>DeploymentAcceptor</code> over a <code>MultiplexSocketTransportProvider</code>.
 * 
 * @author Yanick Duchesne
 */
public class MplexDeploymentAcceptor implements StreamSelector, DeploymentAcceptor{
	
	private MultiplexSocketConnector _connector;
	private ServerContext _context;
	private MultiplexSocketTransportProvider _provider;
	private DeploymentConnector _deployConn;
	private Logger              _logger;
	private Thread              _acceptor;
	
	public MplexDeploymentAcceptor(ServerContext context, 
	                               MultiplexSocketTransportProvider provider,
	                               Logger logger){
	  _context  = context;
		_provider = provider;
		_logger   = logger;
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
  	_deployConn = conn;
  }
  
  ///////// Life-cycle methods /////////
  
	/**
	 * @see DeploymentAcceptor#init()
	 */
	public void init() throws Exception {
		_logger.debug("Creating mplex socket connector to accept deployment connections");
		_connector = _provider.createSocketConnector(this);
	}  
  
  /**
   * @see DeploymentAcceptor#start()
   */
  public void start() throws Exception{
		_logger.debug("Starting mplex deployment acceptor thread");  	
		_acceptor = new Thread(new AcceptorThread(_connector, _deployConn, _context, _logger));
		_acceptor.setName("DeploymentAcceptor");
		_acceptor.setDaemon(true);
		_acceptor.start();
  }
  
  /**
   * @see DeploymentAcceptor#stop()
   */
  public void stop() throws Exception {
    _connector.close();
    _acceptor.interrupt();
  }
}
