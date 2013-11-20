package org.sapia.corus.taskmanager.core;

import org.sapia.corus.client.common.ProgressQueue;

/**
 * An instance of this class wraps a {@link ProgressQueue}, redirecting its
 * output to it.
 * 
 * @author yduchesne
 * 
 */
public class TaskLogProgressQueue implements TaskLog {

  private ProgressQueue progress;

  public TaskLogProgressQueue(ProgressQueue progress) {
    this.progress = progress;
  }

  public void debug(Task<?, ?> task, String msg) {
    progress.debug(format(task, msg));
  }

  public void info(Task<?, ?> task, String msg) {
    progress.info(format(task, msg));
  }

  public void warn(Task<?, ?> task, String msg) {
    progress.warning(format(task, msg));
  }

  public void warn(Task<?, ?> task, String msg, Throwable err) {
    progress.warning(err);
  }

  public void error(Task<?, ?> task, String msg) {
    progress.error(format(task, msg));
  }

  public void error(Task<?, ?> task, String msg, Throwable err) {
    progress.error(err);
  }

  private String format(Task<?, ?> task, String msg) {
    return task.getName() + " >> " + msg;
  }

  public void close() {
    progress.close();
  }
}
