package org.sapia.corus.taskmanager.core;

import org.apache.log.Logger;

public class RootTaskLog extends LoggerTaskLog{
  
  private TaskLog delegate;

  
  public RootTaskLog(Logger logger, TaskLog delegate) {
    super(logger);
    this.delegate = delegate;
  }
  
  public void close() {
    delegate.close();
  }
  
  public void debug(Task task, String msg) {
    super.debug(task, msg);
    delegate.debug(task, msg);
  }
  
  public void error(Task task, String msg, Throwable err) {
    super.error(task, msg, err);
    delegate.error(task, msg, err);
  }
  
  public void error(Task task, String msg) {
    super.error(task, msg);
    delegate.error(task, msg);
  }
  
  public void info(Task task, String msg) {
    super.info(task, msg);
    delegate.info(task, msg);
  }
  
  public void warn(Task task, String msg, Throwable err) {
    super.warn(task, msg, err);
    delegate.warn(task, msg, err);
  }
  
  public void warn(Task task, String msg) {
    super.warn(task, msg);
    delegate.warn(task, msg);
  }
  

}
