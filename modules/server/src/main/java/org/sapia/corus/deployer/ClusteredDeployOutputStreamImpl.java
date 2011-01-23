package org.sapia.corus.deployer;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.sapia.corus.client.services.deployer.transport.DeployOutputStream;


/**
 * An output stream that is used for clustered deployment.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ClusteredDeployOutputStreamImpl extends DeployOutputStreamImpl {
  DeployOutputStream _next;

  /**
   * Constructor for ClusteredDeployOutputStreamImpl.
   * @param fileName
   * @throws FileNotFoundException
   */
  ClusteredDeployOutputStreamImpl(String absolutePath, String fileName,
                                  DeployerImpl current, DeployOutputStream next)
                           throws FileNotFoundException {
    super(absolutePath, fileName, current);
    _next = next;
  }

  public void close() throws IOException {
    super.flush();
    super.close();
    _next.close();
  }

  /**
   * @see java.io.OutputStream#flush()
   */
  public void flush() throws IOException {
    super.flush();
    _next.flush();
  }

  /**
   * @see java.io.FileOutputStream#write(byte[])
   */
  public void write(byte[] bytes) throws IOException {
    super.write(bytes);
    _next.write(bytes);
  }

  /**
   * @see java.io.FileOutputStream#write(byte[], int, int)
   */
  public void write(byte[] bytes, int offset, int length)
             throws IOException {
    super.write(bytes, offset, length);
    _next.write(bytes, offset, length);
  }

  /**
   * @see java.io.FileOutputStream#write(int)
   */
  public void write(int data) throws IOException {
    super.write(data);
    _next.write(data);
  }
}
