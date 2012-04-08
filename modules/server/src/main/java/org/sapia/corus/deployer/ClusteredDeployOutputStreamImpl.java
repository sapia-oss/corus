package org.sapia.corus.deployer;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.sapia.corus.client.services.deployer.transport.DeployOutputStream;


/**
 * An output stream that is used for clustered deployment.
 *
 * @author Yanick Duchesne
 */
public class ClusteredDeployOutputStreamImpl extends DeployOutputStreamImpl {
	
  private DeployOutputStream next;

  /**
   * Constructor for ClusteredDeployOutputStreamImpl.
   * 
   * @param fileName
   * @throws FileNotFoundException
   */
  ClusteredDeployOutputStreamImpl(String absolutePath, String fileName,
                                  DeployerImpl current, DeployOutputStream next)
                           throws FileNotFoundException {
    super(absolutePath, fileName, current);
    this.next = next;
  }

  public void close() throws IOException {
    super.flush();
    super.close();
    next.close();
  }

  /**
   * @see java.io.OutputStream#flush()
   */
  public void flush() throws IOException {
    super.flush();
    next.flush();
  }

  /**
   * @see java.io.FileOutputStream#write(byte[])
   */
  public void write(byte[] bytes) throws IOException {
    super.write(bytes);
    next.write(bytes);
  }

  /**
   * @see java.io.FileOutputStream#write(byte[], int, int)
   */
  public void write(byte[] bytes, int offset, int length)
             throws IOException {
    super.write(bytes, offset, length);
    next.write(bytes, offset, length);
  }

  /**
   * @see java.io.FileOutputStream#write(int)
   */
  public void write(int data) throws IOException {
    super.write(data);
    next.write(data);
  }
}
