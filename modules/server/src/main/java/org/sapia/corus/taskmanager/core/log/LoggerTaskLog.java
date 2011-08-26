package org.sapia.corus.taskmanager.core.log;

import org.apache.log.Logger;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskLog;

public class LoggerTaskLog implements TaskLog{

  private Logger logger;
  
  public LoggerTaskLog(Logger logger) {
    this.logger = logger;
  }

  public synchronized void debug(Task<?,?> task, String msg) {
    logger.debug(task.getName() + " >> " + msg);
  }
  
  public synchronized void info(Task<?,?> task, String msg) {
    logger.info(task.getName() + " >> " + msg);
  }
  
  public synchronized void warn(Task<?,?> task, String msg) {
    logger.warn(task.getName() + " >> " + msg);
  }
  
  public synchronized void warn(Task<?,?> task, String msg, Throwable err) {
    logger.warn(task.getName() + " >> " + msg);
  }
  
  public synchronized void error(Task<?,?> task, String msg) {
    logger.error(task.getName() + " >> " + msg);
  }
  
  public synchronized void error(Task<?,?> task, String msg, Throwable err) {
    logger.error(task.getName() + " >> " + msg, err);
  }

  public void close() {}
}
