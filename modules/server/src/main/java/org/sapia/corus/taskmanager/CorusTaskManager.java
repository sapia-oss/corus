package org.sapia.corus.taskmanager;

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

}
