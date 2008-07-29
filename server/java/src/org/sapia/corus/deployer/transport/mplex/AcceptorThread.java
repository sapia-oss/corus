package org.sapia.corus.deployer.transport.mplex;

import java.io.UnsupportedEncodingException;
import java.net.Socket;

import org.apache.log.Logger;
import org.sapia.corus.deployer.transport.Deployment;
import org.sapia.corus.deployer.transport.DeploymentConnector;
import org.sapia.ubik.net.mplex.MultiplexSocketConnector;

/**
 * Accepts incoming deployment requests, and dispatches them to
 * the encapsulated <code>DeploymentConnector</code>.
 *
 * @see org.sapia.corus.deployer.transport.DeploymentConnector
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
class AcceptorThread implements Runnable{
  
  private MultiplexSocketConnector _connector;
  private DeploymentConnector       _deployConn;
  private Logger                   _logger;
  
  AcceptorThread(MultiplexSocketConnector connector, DeploymentConnector deployConn, Logger logger){
    _connector = connector;
    _deployConn = deployConn;
    _logger = logger;
  }
  
  /**
   * @see java.lang.Runnable#run()
   */
  public void run() {
    _logger.debug("Deployment acceptor thread started");
    Socket client = null;
    
    byte[] header;
    try{
      header = new byte[MplexDeploymentAcceptor.DEPLOY_STREAM_HEADER.getBytes("UTF-8").length];
    }catch(UnsupportedEncodingException e){
      _logger.error("Could not create header buffer; stopping", e);
      return;
    }
    
    while(true){
      try {
        client = _connector.accept();
      } catch (Exception e) {
        _logger.error("Could not accept client connection; stopping", e);
        return;
      } 
      try {
        client.getInputStream().read(header);
      } catch (Exception e) {
        _logger.error("Could not consume header", e);
        continue;
      }
      
      _deployConn.connect(new Deployment(new SocketConnection(client)));
    }
  }
}
