package org.sapia.corus.taskmanager.core.log;

import org.sapia.corus.client.common.log.LogCallback;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskLog;

/**
 * Implements the {@link LogCallback} interface over the {@link TaskLog} interface.
 * 
 * @author yduchesnes
 *
 */
public class LogCallbackTaskLog implements LogCallback {
  
  private TaskExecutionContext taskContext;
  
  public LogCallbackTaskLog(TaskExecutionContext taskContext) {
    this.taskContext = taskContext;
  }
  
  @Override
  public void debug(String msg) {
    taskContext.debug(msg);
  }
  
  @Override
  public void info(String msg) {
    taskContext.info(msg);
  }

  @Override
  public void error(String msg) {
    taskContext.error(msg);
  }
}
