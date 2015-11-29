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
  
  private TaskExecutionContext context;
  
  /**
   * @param context the {@link TaskExecutionContext} to use for logging.
   */
  public TaskLogCallback(TaskExecutionContext context) {
    this.context = context;
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
  }

}
