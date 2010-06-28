package org.sapia.corus.client.services.deployer.transport;

import java.io.IOException;
import java.io.OutputStream;



/**
 * Adapts a <code>DeployOutputStream</code> to an <code>OutputStream</code>.
 * 
 * @author Yanick Duchesne
 */
public class DeployOsAdapter extends OutputStream {
  DeployOutputStream _os;

  public DeployOsAdapter(DeployOutputStream os) {
    _os = os;
  }

  /**
   * @see java.io.OutputStream#close()
   */
  public void close() throws IOException {
    _os.close();
  }

  /**
   * @see java.io.OutputStream#flush()
   */
  public void flush() throws IOException {
    _os.flush();
  }

  /**
   * @see java.io.OutputStream#write(byte[], int, int)
   */
  public void write(byte[] arg0, int arg1, int arg2) throws IOException {
    _os.write(arg0, arg1, arg2);
  }

  /**
   * @see java.io.OutputStream#write(byte[])
   */
  public void write(byte[] arg0) throws IOException {
    _os.write(arg0);
  }

  /**
   * @see java.io.OutputStream#write(int)
   */
  public void write(int arg0) throws IOException {
    _os.write(arg0);
  }
}
