package org.sapia.corus.taskmanager;

import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;

/**
 * Implements the {@link LogCallback} interface by logging to a task log.
 * 
 * @author yduchesne
 *
 */
public class TaskLogCallback implements LogCallback {
  
  private boolean failsafe = true;
  private TaskExecutionContext context;
  
  /**
   * @param context the {@link TaskExecutionContext} to use for logging.
   */
  public TaskLogCallback(TaskExecutionContext context) {
    this.context = context;
  }
  
  /**
   * Sets this instance's {@link #failsafe} flag to true.
   * 
   * @return this instance.
   * @see #failfast().
   */
  public TaskLogCallback failsafe() {
    failsafe = true;
    return this;
  }
  
  /**
   * Sets this instances {@link #failsafe} flag to false (an {@link IllegalStateException} will be 
   * thrown as soon as the {@link #error(String)} method is called).
   * 
   * @return this instance.
   */
  public TaskLogCallback failfast() {
    failsafe = false;
    return this;
  }
  
  @Override
  public void debug(String msg) {
    context.debug(msg);
  }
  
  @Override
  public void info(String msg) {
    context.info(msg);
  }
  
  @Override
  public void error(String msg) {
    context.error(msg);
    if (!failsafe) {
      throw new IllegalStateException(msg);
    }
  }

}
