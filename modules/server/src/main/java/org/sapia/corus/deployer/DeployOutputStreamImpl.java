package org.sapia.corus.deployer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.services.deployer.transport.DeployOutputStream;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;

/**
 * An output stream that is used for deployment.
 * 
 * @author Yanick Duchesne
 */
public class DeployOutputStreamImpl extends FileOutputStream implements DeployOutputStream {

  private DeploymentHandler handler;
  private boolean closed;
  private File destFile;
  private DeploymentMetadata meta;
  private ProgressQueue queue;

  /**
   * @param destFile
   *          the {@link File} to upload to.
   * @param meta
   *          the {@link DeploymentMetadata} corresponding to the uploaded file.
   * @param handler
   *          the {@link DeploymentHandler} to notify once upload has completed.
   * @throws FileNotFoundException
   *           if the destination file could not be opened.
   */
  public DeployOutputStreamImpl(File destFile, DeploymentMetadata meta, DeploymentHandler handler) throws FileNotFoundException {
    super(destFile);
    this.destFile = destFile;
    this.meta = meta;
    this.handler = handler;
  }

  @Override
  public void close() throws IOException {
    if (closed) {
      return;
    }
    super.flush();
    super.close();
    if (handler != null) {
      queue = handler.completeDeployment(meta, destFile);
    }
    closed = true;
  }

  @Override
  public ProgressQueue getProgressQueue() {
    if (queue == null) {
      throw new IllegalStateException("progress queue not available");
    }
    return queue;
  }
}
