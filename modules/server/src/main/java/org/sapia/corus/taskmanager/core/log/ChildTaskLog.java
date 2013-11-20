package org.sapia.corus.taskmanager.core.log;

import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskLog;

/**
 * A {@link TaskLog} that wraps another one and delegates the method calls to it
 * (EXCEPT FOR THE {@link #close()} METHOD - which does nothing).
 * <p>
 * This task log should be used in the context of executing "children" tasks (a
 * child task's execution is triggered by another task).
 * 
 * @author yduchesne
 * 
 */
public class ChildTaskLog implements TaskLog {

  private TaskLog delegate;

  public ChildTaskLog(TaskLog delegate) {
    this.delegate = delegate;
  }

  /**
   * Does nothing.
   */
  public void close() {
  }

  public void debug(Task<?, ?> task, String msg) {
    delegate.debug(task, msg);
  }

  public void error(Task<?, ?> task, String msg, Throwable err) {
    delegate.error(task, msg, err);
  }

  public void error(Task<?, ?> task, String msg) {
    delegate.error(task, msg);
  }

  public void info(Task<?, ?> task, String msg) {
    delegate.info(task, msg);
  }

  public void warn(Task<?, ?> task, String msg, Throwable err) {
    delegate.warn(task, msg, err);
  }

  public void warn(Task<?, ?> task, String msg) {
    delegate.warn(task, msg);
  }

}
