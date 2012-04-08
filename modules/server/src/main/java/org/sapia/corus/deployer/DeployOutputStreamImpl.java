package org.sapia.corus.deployer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.services.deployer.transport.DeployOutputStream;


/**
 * An output stream that is used for deployment.
 *
 * @author Yanick Duchesne
 */
public class DeployOutputStreamImpl extends FileOutputStream implements DeployOutputStream {
	
  private DeployerImpl  deployer;
  private boolean 			closed;
  private String        fileName;
  private ProgressQueue queue;

  /**
   * Constructor for DeployOutputStream.
   * @param fileName
   * @throws FileNotFoundException
   */
  public DeployOutputStreamImpl(String absolutePath, String fileName,
                         DeployerImpl deployer) throws FileNotFoundException {
    super(absolutePath);
    this.deployer = deployer;
    this.fileName = fileName;
  }

  public void close() throws IOException {
  	if(closed) return;
    try {
      super.flush();

      if (deployer != null) {
        queue = deployer.completeDeployment(fileName);
      }
    } finally {
      super.close();
    }
    closed = true;
  }
  
  /**
   * @see org.sapia.corus.client.services.deployer.transport.DeployOutputStream#getProgressQueue()
   */
  public ProgressQueue getProgressQueue() {
    if (queue == null) {
      throw new IllegalStateException("progress queue not available");
    }

    return queue;
  }
}
