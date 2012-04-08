package org.sapia.corus.deployer.transport.mplex;

import java.io.UnsupportedEncodingException;
import java.net.Socket;

import org.apache.log.Logger;
import org.sapia.corus.client.services.deployer.transport.mplex.MplexDeploymentClient;
import org.sapia.corus.core.ServerContext;
import org.sapia.corus.deployer.transport.Deployment;
import org.sapia.corus.deployer.transport.DeploymentConnector;
import org.sapia.ubik.net.mplex.MultiplexSocketConnector;

/**
 * Accepts incoming deployment requests, and dispatches them to
 * the encapsulated {@link DeploymentConnector}.
 *
 * @author Yanick Duchesne
 *
 */
class AcceptorThread implements Runnable{
  
  private MultiplexSocketConnector connector;
  private DeploymentConnector      deployConn;
  private ServerContext            serverContext;
  private Logger                   logger;
  
  AcceptorThread(
      MultiplexSocketConnector connector, 
      DeploymentConnector 		 deployConn, 
      ServerContext 					 context, 
      Logger 									 logger){
    this.connector = connector;
    this.deployConn = deployConn;
    this.serverContext = context;
    this.logger = logger;
  }
  
  /**
   * @see java.lang.Runnable#run()
   */
  public void run() {
    logger.debug("Deployment acceptor thread started");
    Socket client = null;
    
    byte[] header;
    try{
      header = new byte[MplexDeploymentClient.DEPLOY_STREAM_HEADER.getBytes("UTF-8").length];
    }catch(UnsupportedEncodingException e){
      logger.error("Could not create header buffer; stopping", e);
      return;
    }
    
    while(true){
      try {
        client = connector.accept();
      } catch (Exception e) {
        logger.info("Could not accept client connection; server probably shutting down", e);
        return;
      } 
      try {
        client.getInputStream().read(header);
      } catch (Exception e) {
        logger.error("Could not consume header", e);
        continue;
      }
      
      deployConn.connect(new Deployment(serverContext, new SocketConnection(client)));
    }
  }
}
