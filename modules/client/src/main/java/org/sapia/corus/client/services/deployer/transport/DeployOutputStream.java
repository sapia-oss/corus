package org.sapia.corus.client.services.deployer.transport;

import java.io.IOException;

import org.sapia.corus.client.common.ProgressQueue;

/**
 * Specifies the behavior an output stream that is used for deployment.
 * 
 * @author Yanick Duchesne
 */
public interface DeployOutputStream extends java.rmi.Remote {

  void close() throws IOException;

  void flush() throws IOException;

  void write(byte[] b) throws IOException;

  void write(byte[] b, int off, int len) throws IOException;

  void write(int b) throws IOException;
  
  /**
   * Actually deploys the uploaded artifact to Corus. This method calls {@link #flush()} and
   * {@link #close()} internally before performing the deployment.
   * 
   * @throws IOException if an I/O error occurs while committing.
   */
  ProgressQueue commit() throws IOException;
}
