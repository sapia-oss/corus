package org.sapia.corus.deployer.transport;

import org.sapia.corus.core.ServerContext;
import org.sapia.corus.deployer.transport.http.HttpDeploymentAcceptor;
import org.sapia.ubik.rmi.server.transport.http.HttpTransportProvider;

/**
 * @author Yanick Duchesne
 */
public class DeploymentProcessor {

  private DeploymentAcceptor acceptor;
  private DeploymentConnector connector;
  private ServerContext context;

  public DeploymentProcessor(DeploymentConnector connector, ServerContext context) {
    this.connector = connector;
    this.context = context;
  }

  public void init() throws Exception {
    HttpTransportProvider provider = (HttpTransportProvider) context.getTransport().getTransportProvider();
    acceptor = new HttpDeploymentAcceptor(context, provider);
    acceptor.registerConnector(connector);
    acceptor.init();
  }

  public void start() throws Exception {
    acceptor.start();
  }

  public void dispose() {
    try {
      acceptor.stop();
    } catch (Exception e) {
      // noop
    }

  }
}
