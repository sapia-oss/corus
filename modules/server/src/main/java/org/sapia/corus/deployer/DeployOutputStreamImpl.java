package org.sapia.corus.deployer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.services.deployer.transport.DeployOutputStream;
import org.sapia.corus.client.services.deployer.transport.DeploymentMetadata;
import org.sapia.ubik.util.Assertions;

/**
 * An output stream that is used for deployment.
 * 
 * @author Yanick Duchesne
 */
public class DeployOutputStreamImpl extends FileOutputStream implements DeployOutputStream {
  
  private static final int BYTES_FOR_INT = 1;

  private Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());
  private DeploymentHandler handler;
  private boolean closed;
  private File destFile;
  private DeploymentMetadata meta;
  private ProgressQueue queue;
  private int written;

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
  public void write(byte[] b) throws IOException {
    super.write(b);
    written += b.length;
  }
  
  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    super.write(b, off, len);
    written += len;
  }
  
  @Override
  public void write(int b) throws IOException {
    super.write(b);
    written += BYTES_FOR_INT;
  }
  
  @Override
  public void close() throws IOException {
    if (closed) {
      return;
    }
    closed = true;
    log.debug(String.format("%s bytes written", written));
    try {
      super.flush();
    } catch (IOException e) {
    }
    super.close();
  }
  
  @Override
  public ProgressQueue commit() throws IOException {
    close();
    log.debug("Committing deployment");
    Assertions.illegalState(queue != null, "Deployment already committed");
    return handler.completeDeployment(meta, destFile);
  }
  
  /**
   * @return the number of bytes written.
   */
  public int getBytesWritten() {
    return written;
  }

}
