package org.sapia.corus.taskmanager.core;

import org.sapia.corus.core.ServerContext;
import org.sapia.corus.core.ServerContextImpl;
import org.sapia.corus.taskmanager.core.log.ChildTaskLog;

/**
 * Encapsulates state pertaining to the execution of a given task.
 * 
 * @author yduchesne
 *
 */
public class TaskExecutionContext {

  private Task<?,?> task;
  
  private InternalTaskLog log;
  private TaskManager taskManager;
  private ServerContext serverContext;
  
  public TaskExecutionContext(Task<?,?> t, InternalTaskLog log, ServerContext ctx, TaskManager taskMan) {
    this.task = t;
    this.log = log;
    this.serverContext = ctx;
    this.taskManager = new InnerTaskManager(t, log, taskMan);
  }

  /**
   * @return the {@link Task} associated to this instance.
   */
  public Task<?,?> getTask() {
    return task;
  }
  
  /**
   * @return this instance's {@link TaskLog}
   */
  public TaskLog getLog() {
    return log;
  }
  
  /**
   * @return this instance's {@link ServerContextImpl}
   */
  public ServerContext getServerContext() {
    return serverContext;
  }
  
  /**
   * @return this instance's {@link TaskManager}
   */
  public TaskManager getTaskManager() {
    return taskManager;
  }

  public void debug(String msg){
    log.debug(task, msg);
  }
  
  public void info(String msg){
    log.info(task, msg);
  }
  
  public void warn(String msg){
    log.warn(task, msg);
  }
  
  public void warn(String msg, Throwable err){
    log.warn(task, msg, err);
  }
  
  public void error(String msg){
    log.error(task, msg);
  }
  
  public void error(String msg, Throwable err){
    log.error(task, msg, err);
  }
  
  public void error(Throwable err){
    log.error(task, "Error caught", err);
  }
  
  public void close(){
    if(log != null) log.close();
  }
  
  ///////////// Inner task manager impl
  
  static class InnerTaskManager implements TaskManager{
    
    TaskManager owner;
    Task<?,?> task;
    InternalTaskLog log;
    
    public InnerTaskManager(Task<?,?> task, InternalTaskLog log, TaskManager delegate) {
      this.task = task;
      this.owner = delegate;
      this.log = log;
    }
    
    @Override
    public void registerThrottle(ThrottleKey key, Throttle throttle) {
      owner.registerThrottle(key, throttle);
    }
    
    public <R,P> void execute(Task<R,P> child, P param) {
      execute(child, param, SequentialTaskConfig.create());
    }
    
    public <R,P> void execute(Task<R,P> child, P param, SequentialTaskConfig conf) {
      if(conf.getLog() == null){
        conf.setLog(log.getTaskLog());
      }
      owner.execute(child, param, conf);
    }
    
    public <R,P> FutureResult<R> executeAndWait(Task<R,P> child, P param) {
      return executeAndWait(child, param, new TaskConfig());
    }
    
    public <R,P> FutureResult<R> executeAndWait(Task<R,P> child, P param, TaskConfig conf) {
      task.addChild(child);
      if(conf.getLog() == null){
        conf.setLog(new ChildTaskLog(log.getTaskLog()));
      }
      return owner.executeAndWait(child, param, conf);
    }
    
    public <R,P> void executeBackground(Task<R,P> child, P param, BackgroundTaskConfig conf) {
      owner.executeBackground(child, param, conf);
    }
    
  }
}
