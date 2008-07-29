package org.sapia.corus.interop.helpers;

import org.sapia.corus.interop.ConfirmShutdown;
import org.sapia.corus.interop.Poll;
import org.sapia.corus.interop.Restart;
import org.sapia.corus.interop.Status;

import java.util.List;


/**
 * This insterface can conveniently implemented by servers that handle
 * Corus's interoperability protocol. The interface specifies callbacks that
 * are called for each possible request that can be send by Corus clients.
 *
 * @see org.sapia.corus.interop.helpers.ServerStatelessSoapStreamHelper
 *
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface RequestListener {
  /**
   * Called when a dynamic process confirms that it has proceeded to its own shutdown.
   *
   * @param proc a <code>Process</code> object, encapsulating the corus process ID of the request's originator, and
   * a request identifier.
   * @param confirm a <code>ConfirmShutdown</code> command instance.
   * @throws Exception if an error occurs when processing the given command.
   */
  public void onConfirmShutdown(org.sapia.corus.interop.Process proc,
                                ConfirmShutdown confirm)
                         throws Exception;

  /**
   * Called when a dynamic process notifies its corus server about its status.
   *
   * @param proc a <code>Process</code> object, encapsulating the corus process ID of the request's originator, and
   * a request identifier.
   * @param stat a <code>Status</code> command instance.
   * @throws Exception if an error occurs when processing the given command.
   * @return the <code>List</code> of commands that were pending in the process queue, within the
   * corus server.
   */
  public List onStatus(org.sapia.corus.interop.Process proc, Status stat)
                throws Exception;

  /**
   * Called when a dynamic process polls its corus server.
   *
   * @param proc a <code>Process</code> object, encapsulating the corus process ID of the request's originator, and
   * a request identifier.
   * @param poll a <code>Poll</code> command instance.
   * @throws Exception if an error occurs when processing the given command.
   * @return the <code>List</code> of commands that were pending in the process queue, within the
   * corus server.
   */
  public List onPoll(org.sapia.corus.interop.Process proc, Poll poll)
              throws Exception;

  /**
   * Called when a dynamic process notifies its corus server that it wishes to be restarted.
   *
   * @param proc a <code>Process</code> object, encapsulating the corus process ID of the request's originator, and
   * a request identifier.
   * @param res a <code>Restart</code> command instance.
   * @throws Exception if an error occurs when processing the given command.
   */
  public void onRestart(org.sapia.corus.interop.Process proc, Restart res)
                 throws Exception;
}
