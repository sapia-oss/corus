package org.sapia.corus.interop.api;

import org.sapia.corus.interop.Status;


/**
 * This interface can be implemented by application modules that
 * wish to provided status information to the corus server.
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface StatusRequestListener {
  /**
   * This method is a callback that allows applications to publish
   * status information to their corus server. Applications add
   * information to the status using the <code>addContext()</code>
   * method.
   *
   * @param status a <code>Status</code> instance.
   */
  public void onStatus(Status status);
}
