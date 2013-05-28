package org.sapia.corus.taskmanager.core;

import org.sapia.corus.taskmanager.core.log.NullTaskLog;

/**
 * An instance of this class is internally used by a {@link TaskExecutionContextImpl} to route
 * task logging output to the task manager's task log, and to the task's log itself.
 * 
 * @author yduchesne
 *
 */
public class InternalTaskLog implements TaskLog{

  /* the TaskManager's log */
  private TaskLog globalTaskLog;

  /* the Task's log */
  private TaskLog taskLog;

  public InternalTaskLog(TaskLog global, TaskLog specific) {
    this.globalTaskLog = global;
    this.taskLog = specific;
  }

  /**
   * This constructor internally assigns a {@link NullTaskLog} as the task-specific log.
   * 
   * @param global the task manager's {@link TaskLog}
   */
  public InternalTaskLog(TaskLog global) {
    this.globalTaskLog = global;
    this.taskLog = new NullTaskLog();
  }
  
  /**
   * @return the {@link TaskLog} orginally assigned to the task.
   */
  public TaskLog getTaskLog() {
    return taskLog;
  }
  
  /**
   * @return the task manager's {@link TaskLog}
   */
  public TaskLog getGlobalTaskLog() {
    return globalTaskLog;
  }
  
  @Override
  public void close() {
    taskLog.close();
    globalTaskLog.close();
  }

  @Override
  public void debug(Task<?,?> task, String msg) {
    taskLog.debug(task, msg);
    globalTaskLog.debug(task, msg);
  }

  @Override
  public void error(Task<?,?> task, String msg, Throwable err) {
    taskLog.error(task, msg, err);
    globalTaskLog.error(task, msg, err);
  }

  @Override
  public void error(Task<?,?> task, String msg) {
    taskLog.error(task, msg);
    globalTaskLog.error(task, msg);
  }

  @Override
  public void info(Task<?,?> task, String msg) {
    taskLog.info(task, msg);
    globalTaskLog.info(task, msg);
  }

  @Override
  public void warn(Task<?,?> task, String msg, Throwable err) {
    taskLog.warn(task, msg, err);
    globalTaskLog.warn(task, msg, err);
  }

  @Override
  public void warn(Task<?,?> task, String msg) {
    taskLog.warn(task, msg);
    globalTaskLog.warn(task, msg);
  }
}
