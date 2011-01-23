package org.sapia.corus.taskmanager.core;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.sapia.corus.core.ServerContext;

public class TaskManagerImpl implements TaskManager{
  
  private Timer           background;
  private ExecutorService threadpool;
  private ServerContext   serverContext;
  private TaskLog         globalTaskLog;
  
  public TaskManagerImpl(TaskLog globalTaskLog, ServerContext serverContext) {
    this.globalTaskLog = globalTaskLog;
    this.serverContext = serverContext;
    this.background = new Timer("TaskManagerDaemon", true);
    this.threadpool = Executors.newCachedThreadPool();
  }
  
  public void execute(Task task) {
    this.execute(task, SequentialTaskConfig.create());
  }
  
  public void execute(final Task task, final SequentialTaskConfig conf) {
    if(task.isMaxExecutionReached()){
      TaskExecutionContext ctx = createExecutionContext(task, conf);
      try{
        task.onMaxExecutionReached(ctx);
      }catch(Throwable err){
        ctx.error(err);
      }finally{
        task.cleanup(ctx);
      }
    }
    else{
      threadpool.execute(new Runnable(){
        public void run() {
          TaskExecutionContext ctx = createExecutionContext(task, conf);
          try{
            Object result = task.execute(ctx);
            if(conf.getListener() != null){
              conf.getListener().executionSucceeded(task, result);
            }
          }catch(Throwable t){
            ctx.error("Problem occurred executing task", t);
            if(conf.getListener() != null){
              conf.getListener().executionFailed(task, t);
            }
          }finally{
            task.incrementExecutionCount();
            task.cleanup(ctx);
          }
        }
      });
    }
  }
  
  public FutureResult executeAndWait(Task task) {
    return executeAndWait(task, new TaskConfig());
  }
  
  public FutureResult executeAndWait(final Task task, final TaskConfig conf) {
    final FutureResult result = new FutureResult();
    if(task.isMaxExecutionReached()){
      TaskExecutionContext ctx = createExecutionContext(task, conf);
      Object value = null;
      try{
        task.onMaxExecutionReached(ctx);
      }catch(Throwable err){
        value = err;
      }finally{
        task.incrementExecutionCount();
        task.cleanup(ctx);
        result.completed(value);
      }
    }
    else{
      final TaskExecutionContext ctx = createExecutionContext(task, conf);
      threadpool.execute(new Runnable(){
        public void run() {
          Object value = null;
          try{
            value = task.execute(ctx);
          }catch(Throwable t){
            value = t;
          }finally{
            task.incrementExecutionCount();
            task.cleanup(ctx);
            result.completed(value);
          }
        }
      });
    }
    return result;
  }
  
  public void executeBackground(final Task task, final BackgroundTaskConfig config){
    background.schedule(new TimerTask(){  
      @Override
      public void run() {
        if(task.isAborted()){
          super.cancel();
          background.purge();
          if(config.getListener() != null){
            config.getListener().executionAborted(task);
          }
        }
        else if(task.isMaxExecutionReached()){
          TaskExecutionContext ctx = createExecutionContext(task, config);
          try{
            task.onMaxExecutionReached(ctx);
          }catch(Throwable err){
            ctx.error("Error terminating task");
          }
          super.cancel();
          background.purge();
          if(config.getListener() != null){
            config.getListener().executionAborted(task);
          }
        }
        else{
          TaskExecutionContext ctx = createExecutionContext(task, config);
          try{
            task.execute(ctx);
          }catch(Throwable t){
            ctx.error("Problem occurred executing task", t);
          }finally{
            task.incrementExecutionCount();
          }
        }
      }
    }, config.getExecDelay(), config.getExecInterval());
  }
  
  public void shutdown(){
    threadpool.shutdown();
  }
  
  private InternalTaskLog wrapLogFor(Task task, TaskLog log){
    if(log == null){
      return new InternalTaskLog(globalTaskLog);
    }
    else{
      return new InternalTaskLog(globalTaskLog, log);
    }
  }
  
  private TaskExecutionContext createExecutionContext(Task task, TaskConfig config){
    return new TaskExecutionContext(
        task,
        wrapLogFor(task, config.getLog()), 
        serverContext, 
        this);
  }

}
