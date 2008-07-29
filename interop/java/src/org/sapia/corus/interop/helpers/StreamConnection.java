package org.sapia.corus.interop.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Specifies the behavior of stream-based connections used by the
 * <code>ClientStatelessSoapStreamHelper</code>.
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface StreamConnection {
  /**
   * Returns the input stream that is used to process the response coming
   * from the endpoint to which this instance connects.
   *
   * @return an <code>InputStream</code>
   * @throws IOException if a problem occurs when returning the stream.
   */
  public InputStream getInputStream() throws IOException;

  /**
   * Returns the output stream that is used to send a response
   * to the endpoint to which this instance connects.
   *
   * @return an <code>InputStream</code>
   * @throws IOException if a problem occurs when returning the stream.
   */
  public OutputStream getOutputStream() throws IOException;

  /**
   * Closes this instance.
   *
   * @throws IOException if a problem occurs when closing this instance.
   */
  public void close() throws IOException;
}
