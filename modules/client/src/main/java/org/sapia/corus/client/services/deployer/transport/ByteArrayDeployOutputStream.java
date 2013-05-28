package org.sapia.corus.client.services.deployer.transport;

import java.io.ByteArrayOutputStream;

import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.ProgressQueueImpl;

/**
 * {@link DeployOutputStream} implementation based on the {@link ByteArrayOutputStream} class.
 * <p>
 * Use for testing purposes.
 * 
 * @author yduchesne
 *
 */
public class ByteArrayDeployOutputStream extends ByteArrayOutputStream implements DeployOutputStream {

  @Override
  public ProgressQueue getProgressQueue() {
    return new ProgressQueueImpl();
  }
}
