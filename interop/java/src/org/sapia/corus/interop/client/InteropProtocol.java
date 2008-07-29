package org.sapia.corus.interop.client;

import org.sapia.corus.interop.Status;
import org.sapia.corus.interop.soap.FaultException;

import java.io.IOException;

import java.util.List;


/**
 * This interface specifies a facade intended to hide the details of the
 * communication between a dynamic VM and its corus server.
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface InteropProtocol {
  /**
   * Sets this instance's <code>Log</code>.
   *
   * @param log a  <code>Log</code> instance.
   */
  public void setLog(Log log);

  /**
   * Polls the corus server to which this instance connects.
   *
   * @return a <code>List<code> of commands returned by the corus server.
   * @throws FaultException if the corus server a generated a SOAP fault.
   * @throws IOException if a problem occurs while internally sending the request to
   * the corus server.
   */
  public List poll() throws FaultException, IOException;

  /**
   * Sends the given status to the corus server to which this instance connects.
   *
   * @param stat A <code>Status</code> object.
   * @return A <code>List<code> of commands returned by the corus server.
   * @throws FaultException if the corus server a generated a SOAP fault.
   * @throws IOException if a problem occurs while internally sending the request to
   * the corus server.
   */
  public List sendStatus(Status stat) throws FaultException, IOException;

  /**
   * Polls the corus server and sends the given status to the corus
   * server to which this instance connects.
   *
   * @param stat A <code>Status</code> object.
   * @return A <code>List<code> of commands returned by the corus server.
   * @throws FaultException if the corus server a generated a SOAP fault.
   * @throws IOException if a problem occurs while internally sending the request to
   * the corus server.
   */
  public List pollAndSendStatus(Status stat) throws FaultException, IOException;

  /**
   * Sends a restart request to the corus server.
   *
   * @throws FaultException if the corus server a generated a SOAP fault.
   * @throws IOException if a problem occurs while internally sending the request to
   * the corus server.
   */
  public void restart() throws FaultException, IOException;

  /**
   * Sends a shutdown confirmation to the corus server.
   *
   * @throws FaultException if the corus server a generated a SOAP fault.
   * @throws IOException if a problem occurs while internally sending the request to
   * the corus server.
   */
  public void confirmShutdown() throws FaultException, IOException;
}
