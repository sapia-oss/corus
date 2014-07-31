package org.sapia.corus.deployer;

import java.io.IOException;

import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.ProgressQueueImpl;
import org.sapia.corus.client.services.deployer.transport.DeployOutputStream;

/**
 * A {@link DeployOutputStream} implementation that does nothing.
 * 
 * @author yduchesne
 * 
 */
class NullDeployOutputStream implements DeployOutputStream {

  @Override
  public void write(byte[] b) throws IOException {
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
  }

  @Override
  public void write(int b) throws IOException {
  }

  @Override
  public void close() throws IOException {
  }

  @Override
  public void flush() throws IOException {
  }
  
  @Override
  public ProgressQueue commit() throws IOException {
     return new ProgressQueueImpl();
  }

}
