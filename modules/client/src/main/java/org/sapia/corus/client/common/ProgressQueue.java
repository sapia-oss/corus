package org.sapia.corus.client.common;

import java.util.List;



/**
 * This interface is the remote interface for progress queues. A {@link ProgressQueue}
 * hold {@link ProgressMsg} instances.
 *
 * @author Yanick Duchesne
 */
public interface ProgressQueue extends java.rmi.Remote {
  /**
   * Returns <code>true</code> if there are other
   * progress messages in the queue.
   *
   * @return <code>true</code> if there are pending {@link ProgressMsg}
   * objects in the queue.
   */
  public boolean hasNext();

  /**
   * Returns the next batch of progress messages.
   *
   * @return a list of {@link ProgressMsg}.
   */
  public List<ProgressMsg> next();
  
  /**
   * Returns the list of pending messages held within this queue. 
   * If no messages are pending, this method blocks until new messages
   * are added.
   * 
   * @return the next {@link ProgressMsg} instances held within this instance - 
   * list will be empty if the {@link #close()} method has been concurrently 
   * called on this instance.
   */
	public List<ProgressMsg> fetchNext();
  

  /**
   * Adds the given message to this queue.
   *
   * @param msg a {@link ProgressMsg} instance.
   */
  public void addMsg(ProgressMsg msg);

  /**
   * Adds the given message with "debug" verbosity.
   * <p>
   * The given message object is wrapped in a {@link ProgressMsg}
   * instance.
   * 
   * @see ProgressMsg
   * @param msg an <code>Object</code>.
   */
  public void debug(Object msg);

	/**
	 * Adds the given message with the "verbose" level.
	 * 
	 * @see #debug(Object)
	 */
  public void verbose(Object msg);

	/**
	 * Adds the given message with "info" verbosity.
	 * 
   * @see #debug(Object)
   */
  public void info(Object msg);

	/**
	 * Adds the given message with "warning" verbosity.
	 * 
   * @see #debug(Object)
	 */
  public void warning(Object msg);

	/**
	 * Adds the given message with "error" verbosity.
	 * 
   * @see #debug(Object)
	 */
  public void error(Object msg);

	/**
	 * Closes this instance (which should not be used thereafter).
	 */
  public void close();
  
  /**
   * @return <code>true</code> if the {@link #close()} method has
   * been called on this instance.
   */
  public boolean isClosed();
}
