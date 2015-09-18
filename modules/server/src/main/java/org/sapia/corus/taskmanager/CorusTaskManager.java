package org.sapia.corus.taskmanager;

import java.util.List;

import org.sapia.corus.client.common.ProgressMsg;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.taskmanager.core.TaskManager;

/**
 * Extends the {@link TaskManager} interface by adding the
 * {@link #getProgressQueue(int)} method.
 * 
 * @author yduchesne
 * 
 */
public interface CorusTaskManager extends TaskManager {

  public static final String ROLE = CorusTaskManager.class.getName();

  /**
   * @param level
   *          a progress level.
   * @return a {@link ProgressQueue}
   * @see ProgressQueue
   */
  public ProgressQueue getProgressQueue(int level);
  
  /**
   * This method returns the messages whose timestamp is greater than the one given. Note that
   * the timestamp assigned to {@link ProgressMsg} instances corresponds to the value obtained
   * from {@link System#nanoTime()}.
   * 
   * @param level the message level for which to return corresponding progress messages.
   * @param a timestamp the timestamp against which to compare the progress messages candidates to return.
   * @return a {@link List} of {@link ProgressMsg} matching the given level, and whose creation timestamp
   * is greater than the one given.
   */
  public List<ProgressMsg> getBufferedMessages(int level, long timestamp);
  
  
  /**
   * Returns the buffere progress messages corresponding to the given criteria. Clears all messages that
   * have a timestamp greater than the one given, regardless of their level.
   * <p> 
   * Note that the timestamp assigned to {@link ProgressMsg} instances corresponds to the value obtained
   * from {@link System#nanoTime()}.
   * 
   * @param level the message level for which to return corresponding progress messages.
   * @param a timestamp the timestamp against which to compare the progress messages candidates to return.
   * @return a {@link List} of {@link ProgressMsg} matching the given level, and whose creation timestamp
   * is greater than the one given.
   */
  public List<ProgressMsg> clearBufferedMessages(int level, long timestamp);
  
}
