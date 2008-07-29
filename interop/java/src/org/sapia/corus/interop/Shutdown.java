package org.sapia.corus.interop;


/**
 *
 *
 * @author <a href="mailto:jc@sapia-oss.org">Jean-Cedric Desrochers</a>
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">
 *     Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *     <a href="http://www.sapia-oss.org/license.html" target="sapia-license">license page</a>
 *     at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class Shutdown extends AbstractCommand {
  /** Defines the entity that requested the shutdown. */
  private String _theRequestor;

  /**
   * Creates a new Shutdown instance.
   */
  public Shutdown() {
  }

  /**
   * Returns the entity that requested the shutdown command. Some possible values could:
   * <ul>
   * <li><b>corus</b> - when the Corus server discovers that a process has not polled back
   *                     the server for a too long period of time.</li>
   * <li><b>console</b> - when an administrator ask the server to stop a specific process.</li>
   * <li><b>process</b> - when a process managed by Corus explicitly ask to be shutdown
   *                      using the restart command.</li>
   * </ul>
   *
   *
   * @return The entity that requested the shutdown command.
   */
  public String getRequestor() {
    return _theRequestor;
  }

  /**
   * Changes the requestor of this shutdown command.
   *
   * @param aRequestor The new requestor.
   */
  public void setRequestor(String aRequestor) {
    _theRequestor = aRequestor;
  }

  /**
   * Returns a string representation of this shutdown command.
   *
   * @return A string representation of this shutdown command.
   */
  public String toString() {
    StringBuffer aBuffer = new StringBuffer(super.toString());
    aBuffer.append("[requestor=").append(_theRequestor).append("]");

    return aBuffer.toString();
  }
}
