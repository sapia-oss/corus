package org.sapia.corus.taskmanager.core.log;

import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskLog;

/**
 * A "null" {@link TaskLog} implementation.
 * 
 * @author yduchesne
 *
 */
public class NullTaskLog implements TaskLog{

  @Override
  public void close() {
  }

  @Override
  public void debug(Task task, String msg) {
  }

  @Override
  public void error(Task task, String msg, Throwable err) {
  }

  @Override
  public void error(Task task, String msg) {
  }

  @Override
  public void info(Task task, String msg) {
  }

  @Override
  public void warn(Task task, String msg, Throwable err) {
  }

  @Override
  public void warn(Task task, String msg) {
  }
}
