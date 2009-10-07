package org.sapia.corus.taskmanager.core;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log.Logger;
import org.sapia.corus.ServerContext;

public class TaskManagerImpl implements TaskManager{
  
  private Timer           background;
  private ExecutorService threadpool;
  private ServerContext   serverContext;
  private Logger          logger;
  
  public TaskManagerImpl(Logger logger, ServerContext serverContext) {
    this(logger, serverContext, 5);
  }
  
  public TaskManagerImpl(Logger logger, ServerContext serverContext, int parallelThreads) {
    this.logger = logger;
    this.serverContext = serverContext;
    this.background = new Timer("TaskManagerDaemon", true);
    this.threadpool = Executors.newCachedThreadPool();
  }
  
  public void execute(Task task) {
    this.execute(task, SequentialTaskConfig.create());
  }
  
  public void execute(final Task task, final SequentialTaskConfig conf) {
    if(task.isMaxExecutionReached()){
      TaskExecutionContext ctx = new TaskExecutionContext(
          task,
          createLogFor(task, conf.getLog()), 
          serverContext, 
          self());
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
          TaskExecutionContext ctx = new TaskExecutionContext(
              task,
              createLogFor(task, conf.getLog()), 
              serverContext, 
              self());
          try{
            Object result = task.execute(ctx);
            if(conf.getListener() != null){
              conf.getListener().executionSucceeded(task, result);
            }
          }catch(Throwable t){
            ctx.getLog().error(task, "Problem occurred executing task", t);
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
      TaskExecutionContext ctx = new TaskExecutionContext(
          task,
          createLogFor(task, conf.getLog()), 
          serverContext, 
          self());
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
      final TaskExecutionContext ctx = new TaskExecutionContext(
          task,
          createLogFor(task, conf.getLog()), 
          serverContext, 
          self());
      
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
          TaskExecutionContext ctx = new TaskExecutionContext(
              task,
              createLogFor(task, config.getLog()), 
              serverContext, 
              self());
          try{
            task.onMaxExecutionReached(ctx);
          }catch(Throwable err){
            ctx.getLog().error(task, "Error terminating task");
          }
          super.cancel();
          background.purge();
          if(config.getListener() != null){
            config.getListener().executionAborted(task);
          }
        }
        else{
          TaskExecutionContext ctx = new TaskExecutionContext(
              task,
              createLogFor(task, config.getLog()), 
              serverContext, 
              self());
          try{
            task.execute(ctx);
          }catch(Throwable t){
            ctx.getLog().error(task, "Problem occurred executing task", t);
          }finally{
            task.incrementExecutionCount();
          }
        }
      }
    }, config.getExecDelay(), config.getExecInterval());
  }
  
  public void fork(final Task task) {
    fork(task, ForkedTaskConfig.create());
  }
  
  public void fork(final Task task, final ForkedTaskConfig config) {
    if(task.isMaxExecutionReached()){
      TaskExecutionContext ctx = new TaskExecutionContext(
          task,
          createLogFor(task, config.getLog()), 
          serverContext, 
          self());
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
          Object value = null;
          TaskExecutionContext ctx = new TaskExecutionContext(
              task,
              createLogFor(task, config.getLog()),
              serverContext, 
              self());
          try{
            value = task.execute(ctx);
          }catch(Throwable t){
            value = t;
          }finally{
            task.incrementExecutionCount();
            if(config.getListener() != null){
              if(value instanceof Throwable){
                config.getListener().executionFailed(task, (Throwable)value);
              }
              else{
                config.getListener().executionSucceeded(task, value);
              }
            }
            task.cleanup(ctx);
          }
        }
      });
    }
  }
  
  public void shutdown(){
    threadpool.shutdown();
  }
  
  protected TaskLog createLogFor(Task task, TaskLog delegate){
    if(task.isRoot()){
      if(delegate == null){
        return new LoggerTaskLog(logger);
      }
      else{
        return new RootTaskLog(logger, delegate);
      }
    }
    else{
      if(delegate == null){
        return new LoggerTaskLog(logger);
      }
      else{
        return new ChildTaskLog(delegate);
      }
    }
  }
  
  private TaskManager self(){
    return this;
  }

}
