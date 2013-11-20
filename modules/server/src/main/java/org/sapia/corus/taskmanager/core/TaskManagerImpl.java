package org.sapia.corus.taskmanager.core;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import org.sapia.corus.core.ServerContext;
import org.sapia.ubik.concurrent.ConfigurableExecutor;
import org.sapia.ubik.concurrent.ConfigurableExecutor.ThreadingConfiguration;
import org.sapia.ubik.concurrent.NamedThreadFactory;

public class TaskManagerImpl implements TaskManager {

  private Timer background;
  private ExecutorService threadpool;
  private ServerContext serverContext;
  private TaskLog globalTaskLog;
  private Map<ThrottleKey, Throttle> throttles = new ConcurrentHashMap<ThrottleKey, Throttle>();

  public TaskManagerImpl(TaskLog globalTaskLog, ServerContext serverContext, ThreadingConfiguration conf) {
    this.globalTaskLog = globalTaskLog;
    this.serverContext = serverContext;
    this.background = new Timer("TaskManagerDaemon", true);
    this.threadpool = new ConfigurableExecutor(conf, NamedThreadFactory.createWith("TaskManager"));
  }

  @Override
  public void registerThrottle(ThrottleKey key, Throttle throttle) {
    throttles.put(key, throttle);
  }

  public <R, P> void execute(Task<R, P> task, P param) {
    this.execute(task, param, SequentialTaskConfig.create());
  }

  public <R, P> void execute(final Task<R, P> task, final P param, final SequentialTaskConfig conf) {
    if (task.isMaxExecutionReached()) {
      TaskExecutionContext ctx = createExecutionContext(task, conf);
      try {
        task.onMaxExecutionReached(ctx);
      } catch (Throwable err) {
        ctx.error(err);
      } finally {
        task.cleanup(ctx);
      }
    } else {
      Runnable toRun = new Runnable() {
        public void run() {
          TaskExecutionContext ctx = createExecutionContext(task, conf);
          try {
            Object result = task.execute(ctx, param);
            if (conf.getListener() != null) {
              conf.getListener().executionSucceeded(task, result);
            }
          } catch (Throwable t) {
            ctx.error("Problem occurred executing task", t);
            if (conf.getListener() != null) {
              conf.getListener().executionFailed(task, t);
            }
          } finally {
            task.incrementExecutionCount();
            task.cleanup(ctx);
          }
        }
      };
      if (task instanceof Throttleable) {
        throttle(((Throttleable) task).getThrottleKey(), toRun);
      } else {
        threadpool.execute(toRun);
      }
    }
  }

  public <R, P> FutureResult<R> executeAndWait(Task<R, P> task, P param) {
    return executeAndWait(task, param, new TaskConfig());
  }

  public <R, P> FutureResult<R> executeAndWait(final Task<R, P> task, final P param, final TaskConfig conf) {
    final FutureResult<R> result = new FutureResult<R>();
    if (task.isMaxExecutionReached()) {
      TaskExecutionContext ctx = createExecutionContext(task, conf);
      Object value = null;
      try {
        task.onMaxExecutionReached(ctx);
      } catch (Throwable err) {
        value = err;
      } finally {
        task.incrementExecutionCount();
        task.cleanup(ctx);
        result.completed(value);
      }
    } else {
      final TaskExecutionContext ctx = createExecutionContext(task, conf);

      Runnable toRun = new Runnable() {
        public void run() {
          Object value = null;
          try {
            value = task.execute(ctx, param);
          } catch (Throwable t) {
            value = t;
          } finally {
            task.incrementExecutionCount();
            task.cleanup(ctx);
            result.completed(value);
          }
        }
      };

      if (task instanceof Throttleable) {
        throttle(((Throttleable) task).getThrottleKey(), toRun);
      } else {
        threadpool.execute(toRun);
      }
    }
    return result;
  }

  public <R, P> void executeBackground(final Task<R, P> task, final P param, final BackgroundTaskConfig config) {
    background.schedule(new TimerTask() {
      @Override
      public void run() {
        if (task.isAborted()) {
          super.cancel();
          background.purge();
          if (config.getListener() != null) {
            config.getListener().executionAborted(task);
          }
        } else if (task.isMaxExecutionReached()) {
          TaskExecutionContext ctx = createExecutionContext(task, config);
          try {
            task.onMaxExecutionReached(ctx);
          } catch (Throwable err) {
            ctx.error("Error terminating task");
          }
          super.cancel();
          background.purge();
          if (config.getListener() != null) {
            config.getListener().executionAborted(task);
          }
        } else {
          TaskExecutionContext ctx = createExecutionContext(task, config);
          try {
            task.execute(ctx, param);
          } catch (Throwable t) {
            ctx.error("Problem occurred executing task", t);
          } finally {
            task.incrementExecutionCount();
          }
        }
      }
    }, config.getExecDelay(), config.getExecInterval());
  }

  public void shutdown() {
    threadpool.shutdown();
  }

  private InternalTaskLog wrapLogFor(Task<?, ?> task, TaskLog log) {
    if (log == null) {
      return new InternalTaskLog(globalTaskLog);
    } else {
      return new InternalTaskLog(globalTaskLog, log);
    }
  }

  private TaskExecutionContext createExecutionContext(Task<?, ?> task, TaskConfig config) {
    return new TaskExecutionContextImpl(task, wrapLogFor(task, config.getLog()), serverContext, this);
  }

  private void throttle(ThrottleKey throttleKey, Runnable toRun) {
    Throttle throttle = throttles.get(throttleKey);
    if (throttle == null) {
      throw new IllegalStateException(String.format("No throttle found for %s", throttleKey.getName()));
    }
    throttle.execute(toRun);
  }

}
