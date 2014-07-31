package org.sapia.corus.deployer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.sapia.corus.client.services.deployer.transport.DeployOutputStream;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;

/**
 * An output stream that is used for clustered deployment.
 * 
 * @author Yanick Duchesne
 */
public class ClusteredDeployOutputStreamImpl extends DeployOutputStreamImpl {

  private DeployOutputStream next;

  /**
   * @param destFile
   *          the {@link File} to upload to.
   * @param meta
   *          the {@link DeploymentMetadata} corresponding to the uploaded file.
   * @param handler
   *          the {@link DeploymentHandler} to notify once upload has completed.
   * @param next
   *          the "next" {@link DeployOutputStream} that is part of the
   *          deployment chain.
   * @throws FileNotFoundException
   *           if the destination file could not be opened.
   */
  ClusteredDeployOutputStreamImpl(File destFile, DeploymentMetadata meta, DeploymentHandler handler, DeployOutputStream next)
      throws FileNotFoundException {
    super(destFile, meta, handler);
    this.next = next;
  }

  @Override
  public void close() throws IOException {
    super.flush();
    super.close();
    next.close();
  }

  @Override
  public void flush() throws IOException {
    super.flush();
    next.flush();
  }

  @Override
  public void write(byte[] bytes) throws IOException {
    super.write(bytes);
    next.write(bytes);
  }

  @Override
  public void write(byte[] bytes, int offset, int length) throws IOException {
    super.write(bytes, offset, length);
    next.write(bytes, offset, length);
  }

  @Override
  public void write(int data) throws IOException {
    super.write(data);
    next.write(data);
  }
}
