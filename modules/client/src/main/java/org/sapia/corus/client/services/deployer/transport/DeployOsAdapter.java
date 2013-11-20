package org.sapia.corus.client.services.deployer.transport;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Adapts a {@link DeployOutputStream} to an {@link OutputStream}.
 * 
 * @author Yanick Duchesne
 */
public class DeployOsAdapter extends OutputStream {

  DeployOutputStream os;

  public DeployOsAdapter(DeployOutputStream os) {
    this.os = os;
  }

  /**
   * @see java.io.OutputStream#close()
   */
  public void close() throws IOException {
    os.close();
  }

  /**
   * @see java.io.OutputStream#flush()
   */
  public void flush() throws IOException {
    os.flush();
  }

  /**
   * @see java.io.OutputStream#write(byte[], int, int)
   */
  public void write(byte[] arg0, int arg1, int arg2) throws IOException {
    os.write(arg0, arg1, arg2);
  }

  /**
   * @see java.io.OutputStream#write(byte[])
   */
  public void write(byte[] arg0) throws IOException {
    os.write(arg0);
  }

  /**
   * @see java.io.OutputStream#write(int)
   */
  public void write(int arg0) throws IOException {
    os.write(arg0);
  }
}
