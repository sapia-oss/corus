package org.sapia.corus.taskmanager.core;

public class TaskConfig {

  private TaskLog log;

  public TaskConfig setLog(TaskLog log) {
    this.log = log;
    return this;
  }

  public TaskLog getLog() {
    return log;
  }

  public static TaskConfig create(TaskLog log) {
    TaskConfig cfg = new TaskConfig();
    cfg.setLog(log);
    return cfg;
  }

}
