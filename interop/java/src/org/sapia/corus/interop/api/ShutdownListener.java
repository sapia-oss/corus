package org.sapia.corus.interop.api;


/**
 * This interface can be implemented by application modules that
 * wish to be notified about shutdown in order to cleanly terminate.
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface ShutdownListener {
  /**
   * Called when the corus server has requested a VM shutdown.
   */
  public void onShutdown();
}
