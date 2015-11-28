package org.sapia.corus.client.services.deployer.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Yanick Duchesne
 */
public class DeploymentClientConnection implements Connection {

  private AbstractDeploymentClient client;

  public DeploymentClientConnection(AbstractDeploymentClient client) {
    this.client = client;
  }
  
  @Override
  public String getRemoteHost() {
    throw new UnsupportedOperationException("getRemoteHost() method not implemented");
  }

  @Override
  public void close() {
    client.close();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return client.getInputStream();
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return client.getOutputStream();
  }

}
