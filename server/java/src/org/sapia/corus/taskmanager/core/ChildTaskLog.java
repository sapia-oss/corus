package org.sapia.corus.taskmanager.core;


public class ChildTaskLog implements TaskLog{
  
  private TaskLog parent;
  
  public ChildTaskLog(TaskLog parent) {
    this.parent = parent;
  }

  public synchronized void debug(Task task, String msg) {
    parent.debug(task, msg);
  }
  
  public synchronized void info(Task task, String msg) {
    parent.info(task, msg);
  }
  
  public synchronized void warn(Task task, String msg) {
    parent.warn(task, msg);
  }
  
  public synchronized void warn(Task task, String msg, Throwable err) {
    parent.warn(task, msg, err);
  }
  
  public synchronized void error(Task task, String msg) {
    parent.error(task, msg);
  }
  
  public synchronized void error(Task task, String msg, Throwable err) {
    parent.error(task, msg, err);
  }
  
  public synchronized void close() {
    if(parent != null){
      parent.close();
    }
  }
}
