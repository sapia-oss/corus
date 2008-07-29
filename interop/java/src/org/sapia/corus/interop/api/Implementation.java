/*
 * Implementation.java
 *
 * Created on October 26, 2005, 9:44 AM
 *
 */

package org.sapia.corus.interop.api;

/**
 * An imlementation of this insterface is provided as a link to the Corus
 * server that started this VM.
 *
 * @author yduchesne
 */
public interface Implementation {
  
  /**
   * Returns the corus process identifier corresponding to the VM.
   *
   * @return the dymamo process ID of this VM, or <code>null</code>
   * if this VM was not started by a corus server.
   */
  public String getCorusPid();

  /**
   * Returns the corus distribution name of this VM.
   *
   * @return the corus distribution name of this VM, or <code>null</code>
   * if this VM was not started by a corus server.
   */
  public String getDistributionName();

  /**
   * Returns the version of this VM's distribution.
   *
   * @return the version of this VM's distribution, or <code>null</code>
   * if this VM was not started by a corus server.
   */
  public String getDistributionVersion();

  /**
   * Returns the root directory of this VM's distribution.
   *
   * @return the root directory of this VM's distribution, or <code>null</code>
   * if this VM was not started by a corus server.
   */
  public String getDistributionDir();

  /**
   * @return the host of the corus server that started this VM, or <code>null</code>
   * if this VM was not started by a corus server.
   */
  public String getCorusHost();

  /**
   * @return the main port of the corus server that started this VM, or <code>-1</code>
   * if this VM was not started by a corus server.
   */
  public int getCorusPort();

  /**
   * @return <code>true</code> if the VM was started by a corus server.
   */
  public boolean isDynamic();

  /**
   * Sends a restart request to the corus server that started this
   * VM. The corus server will trigger a clean shut down of this VM
   * and restart it.
   */
  public void restart();

  /**
   * Shuts down this client. And terminates this VM. Internally
   * calls this client's <code>ShutdownListener</code>s so that the
   * latter can cleanly shut down.
   */
  public void shutdown();
  
  /**
   * Adds a <code>ShutdownListener</code> to this client. The listener
   * is internally kept in a <code>SoftReference</code>, so client applications
   * should keep a reference on the given listener in order to spare the
   * latter from being GC'ed.
   *
   * @param listener a <code>ShutdownListener</code>.
   */
  public void addShutdownListener(ShutdownListener listener);
  
  /**
   * Adds a <code>StatusRequestListener</code> to this client. The listener
   * is internally kept in a <code>SoftReference</code>, so client applications
   * should keep a reference on the given listener in order to spare the
   * latter from being GC'ed.
   *
   * @param listener a <code>StatusRequestListener</code>.
   */
  public void addStatusRequestListener(StatusRequestListener listener);
  
  
}
