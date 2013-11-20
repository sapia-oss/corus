package org.sapia.corus.taskmanager.core;

import org.sapia.corus.core.ServerContext;
import org.sapia.corus.taskmanager.core.log.ChildTaskLog;

/**
 * Encapsulates state pertaining to the execution of a given task.
 * 
 * @author yduchesne
 * 
 */
public class TaskExecutionContextImpl implements TaskExecutionContext {

  private Task<?, ?> task;

  private InternalTaskLog log;
  private TaskManager taskManager;
  private ServerContext serverContext;

  public TaskExecutionContextImpl(Task<?, ?> t, InternalTaskLog log, ServerContext ctx, TaskManager taskMan) {
    this.task = t;
    this.log = log;
    this.serverContext = ctx;
    this.taskManager = new InnerTaskManager(t, log, taskMan);
  }

  @Override
  public Task<?, ?> getTask() {
    return task;
  }

  @Override
  public TaskLog getLog() {
    return log;
  }

  @Override
  public ServerContext getServerContext() {
    return serverContext;
  }

  @Override
  public TaskManager getTaskManager() {
    return taskManager;
  }

  @Override
  public void debug(String msg) {
    log.debug(task, msg);
  }

  @Override
  public void info(String msg) {
    log.info(task, msg);
  }

  @Override
  public void warn(String msg) {
    log.warn(task, msg);
  }

  @Override
  public void warn(String msg, Throwable err) {
    log.warn(task, msg, err);
  }

  @Override
  public void error(String msg) {
    log.error(task, msg);
  }

  @Override
  public void error(String msg, Throwable err) {
    log.error(task, msg, err);
  }

  @Override
  public void error(Throwable err) {
    log.error(task, "Error caught", err);
  }

  @Override
  public void close() {
    if (log != null)
      log.close();
  }

  // /////////// Inner task manager impl

  static class InnerTaskManager implements TaskManager {

    TaskManager owner;
    Task<?, ?> task;
    InternalTaskLog log;

    public InnerTaskManager(Task<?, ?> task, InternalTaskLog log, TaskManager delegate) {
      this.task = task;
      this.owner = delegate;
      this.log = log;
    }

    @Override
    public void registerThrottle(ThrottleKey key, Throttle throttle) {
      owner.registerThrottle(key, throttle);
    }

    public <R, P> void execute(Task<R, P> child, P param) {
      execute(child, param, SequentialTaskConfig.create());
    }

    public <R, P> void execute(Task<R, P> child, P param, SequentialTaskConfig conf) {
      if (conf.getLog() == null) {
        conf.setLog(log.getTaskLog());
      }
      owner.execute(child, param, conf);
    }

    public <R, P> FutureResult<R> executeAndWait(Task<R, P> child, P param) {
      return executeAndWait(child, param, new TaskConfig());
    }

    public <R, P> FutureResult<R> executeAndWait(Task<R, P> child, P param, TaskConfig conf) {
      task.addChild(child);
      if (conf.getLog() == null) {
        conf.setLog(new ChildTaskLog(log.getTaskLog()));
      }
      return owner.executeAndWait(child, param, conf);
    }

    public <R, P> void executeBackground(Task<R, P> child, P param, BackgroundTaskConfig conf) {
      owner.executeBackground(child, param, conf);
    }

  }
}
