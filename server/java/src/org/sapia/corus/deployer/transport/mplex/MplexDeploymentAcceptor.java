package org.sapia.corus.deployer.transport.mplex;

import java.io.UnsupportedEncodingException;

import org.apache.log.Logger;
import org.sapia.corus.deployer.transport.DeploymentAcceptor;
import org.sapia.corus.deployer.transport.DeploymentConnector;
import org.sapia.ubik.net.mplex.MultiplexSocketConnector;
import org.sapia.ubik.net.mplex.StreamSelector;
import org.sapia.ubik.rmi.server.transport.socket.MultiplexSocketTransportProvider;

/**
 * Implements the <code>DeploymentAcceptor</code> over a <code>MultiplexSocketTransportProvider</code>.
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class MplexDeploymentAcceptor implements StreamSelector, DeploymentAcceptor{
	
	public static final String DEPLOY_STREAM_HEADER = "corus-1.0/deployer";
	
	private MultiplexSocketConnector _connector;
	private MultiplexSocketTransportProvider _provider;
	private DeploymentConnector _deployConn;
	private Logger              _logger;
	private Thread              _acceptor;
	
	public MplexDeploymentAcceptor(MultiplexSocketTransportProvider provider,
	                               Logger logger){
		_provider = provider;
		_logger   = logger;
	}
	
	/**
   * @see org.sapia.ubik.net.mplex.StreamSelector#selectStream(byte[])
   */
  public boolean selectStream(byte[] header) {
  	try{
    	String headerStr = new String(header, 0, header.length,  "UTF-8");
  	  return headerStr.startsWith(DEPLOY_STREAM_HEADER);
  	}catch(UnsupportedEncodingException e){
  		return false;
  	}
  }
  
	///////// DeploymentConnector methods /////////  
  
  /**
   * @see org.sapia.corus.deployer.transport.DeploymentAcceptor#registerConnector(DeploymentConnector)
   */
  public void registerConnector(DeploymentConnector conn) {
  	_deployConn = conn;
  }
  
  ///////// Life-cycle methods /////////
  
	/**
	 * @see org.sapia.corus.deployer.transport.DeploymentConnector#init()
	 */
	public void init() throws Exception {
		_logger.debug("Creating mplex socket connector to accept deployment connections");
		_connector = _provider.createSocketConnector(this);
	}  
  
  /**
   * @see org.sapia.corus.deployer.transport.DeploymentConnector#start()
   */
  public void start() throws Exception{
		_logger.debug("Starting mplex deployment acceptor thread");  	
		_acceptor = new Thread(new AcceptorThread(_connector, _deployConn, _logger));
		_acceptor.setName("DeploymentAcceptor");
		_acceptor.setDaemon(true);
		_acceptor.start();
  }
  
  /**
   * @see org.sapia.corus.deployer.transport.DeploymentConnector#stops()
   */
  public void stop() throws Exception {
    _connector.close();
    _acceptor.interrupt();
  }
}
