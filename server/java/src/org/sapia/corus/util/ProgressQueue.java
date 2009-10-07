package org.sapia.corus.util;

import java.util.List;


/**
 * This interface is the remote interface for progress queues.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public interface ProgressQueue extends java.rmi.Remote {
  /**
   * Returns <code>true</code> if there are other
   * progress messages in the queue.
   *
   * @return <code>true</code> if there are pending <code>ProgressMsg</code>
   * objects in the queue.
   */
  public boolean hasNext();

  /**
   * Returns the next batch of progress messages.
   *
   * @return a <code>List</code> of <code>ProgressMsg</code>.
   */
  public List<ProgressMsg> next();
  
  /**
   * Returns the list of pending messages held within this queue. 
   * If no messages are pending, this method blocks until new messages
   * are added.
   * 
   * @return the <code>List</code> of the next <code>ProgressMsg</code>
   * instances held within this instance - list will be empty if the <code>close()</code>
   * method has been concurrently called on this instance.
   */
	public List<ProgressMsg> fetchNext();
  

  /**
   * Adds the given message to this queue.
   *
   * @param msg a <code>ProgressMsg</code> instance.
   */
  public void addMsg(ProgressMsg msg);

  /**
   * Adds the given message with "debug" verbosity.
   * <p>
   * The given message object is wrapped in a <code>ProgessMsg</code>
   * instance.
   * 
   * @see ProgressMsg
   * @param msg an <code>Object</code>.
   */
  public void debug(Object msg);

	/**
	 * Adds the given message with the "verbose" level.
	 * <p>
	 * The given message object is wrapped in a <code>ProgessMsg</code>
	 * instance.
	 * 
	 * @see ProgressMsg
	 * @param msg an <code>Object</code>.
	 */
  public void verbose(Object msg);

	/**
	 * Adds the given message with "info" verbosity.
	 * <p>
	 * The given message object is wrapped in a <code>ProgessMsg</code>
	 * instance.
	 * 
	 * @see ProgressMsg
	 * @param msg an <code>Object</code>.
	 */
  public void info(Object msg);

	/**
	 * Adds the given message with "warning" verbosity.
	 * <p>
	 * The given message object is wrapped in a <code>ProgessMsg</code>
	 * instance.
	 * 
	 * @see ProgressMsg
	 * @param msg an <code>Object</code>.
	 */
  public void warning(Object msg);

	/**
	 * Adds the given message with "error" verbosity.
	 * <p>
	 * The given message object is wrapped in a <code>ProgessMsg</code>
	 * instance.
	 * 
	 * @see ProgressMsg
	 * @param msg an <code>Object</code>.
	 */
  public void error(Object msg);

	/**
	 * Closes this instance (which should not be used thereafter).
	 */
  public void close();
  
  /**
   * @return <code>true</code> if the <code>close()</code> method has
   * been called on this instance.
   */
  public boolean isClosed();
}
