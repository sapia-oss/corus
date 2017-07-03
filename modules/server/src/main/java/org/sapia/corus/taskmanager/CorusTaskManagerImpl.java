package org.sapia.corus.taskmanager;

import java.util.List;

import org.sapia.corus.client.annotations.Bind;
import org.sapia.corus.client.common.ProgressMsg;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.ProgressQueueImpl;
import org.sapia.corus.core.ModuleHelper;
import org.sapia.corus.taskmanager.core.BackgroundTaskConfig;
import org.sapia.corus.taskmanager.core.FutureResult;
import org.sapia.corus.taskmanager.core.ProgressBuffer;
import org.sapia.corus.taskmanager.core.SequentialTaskConfig;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskConfig;
import org.sapia.corus.taskmanager.core.TaskManager;
import org.sapia.corus.taskmanager.core.TaskManagerImpl;
import org.sapia.corus.taskmanager.core.Throttle;
import org.sapia.corus.taskmanager.core.ThrottleKey;
import org.sapia.corus.taskmanager.core.log.LoggerTaskLog;
import org.sapia.ubik.concurrent.ConfigurableExecutor.ThreadingConfiguration;
import org.sapia.ubik.rmi.Remote;
import org.sapia.ubik.util.Condition;
import org.sapia.ubik.util.TimeValue;

/**
 * This module implements the {@link CorusTaskManager} interface.
 * 
 * @author Yanick Duchesne
 */
@Bind(moduleInterface = { TaskManager.class, CorusTaskManager.class })
@Remote(interfaces = {CorusTaskManager.class})
public class CorusTaskManagerImpl extends ModuleHelper implements CorusTaskManager {

  private static final int  CORE_POOL_SIZE      = 15;
  private static final int  MAX_POOL_SIZE       = 100;
  private static final int  MAX_PROGRESS_SIZE   = 50;
  private static final long KEEP_ALIVE_SECONDS  = 30;
  private static final int  WORK_QUEUE_SIZE     = 100;

  private TaskManagerImpl delegate;
  private ProgressQueues  queues    = new ProgressQueues();
  private ProgressBuffer  buffer;

  // --------------------------------------------------------------------------
  // Module interface

  /**
   * @see org.sapia.corus.client.Module#getRoleName()
   */
  public String getRoleName() {
    return CorusTaskManager.ROLE;
  }

  // --------------------------------------------------------------------------
  // Lifecycle

  public void init() throws Exception {
    buffer = new ProgressBuffer(MAX_PROGRESS_SIZE, ProgressMsg.DEBUG);
    ThreadingConfiguration conf = ThreadingConfiguration.newInstance()
        .setCorePoolSize(CORE_POOL_SIZE)
        .setKeepAlive(TimeValue.createSeconds(KEEP_ALIVE_SECONDS))
        .setMaxPoolSize(MAX_POOL_SIZE).setQueueSize(WORK_QUEUE_SIZE);

    delegate = new TaskManagerImpl(new ServerTaskLog(buffer, queues, new LoggerTaskLog(log)), serverContext(), conf);
  }

  @Override
  public void dispose() {
    if (delegate != null) {
      delegate.shutdown();
    }
  }

  // --------------------------------------------------------------------------
  // CorusTaskManager interface

  @Override
  public List<ProgressMsg> getBufferedMessages(final int level, final long timestamp) {
    return buffer.subList(new Condition<ProgressMsg>() {
      @Override
      public boolean apply(ProgressMsg msg) {
        return msg.getStatus() >= level && msg.getTimestamp() > timestamp;
      }
    });
  }
  
  @Override
  public List<ProgressMsg> clearBufferedMessages(int level, final long timestamp) {
    List<ProgressMsg> toReturn = getBufferedMessages(level, timestamp);
    buffer.clear(new Condition<ProgressMsg>() {
      @Override
      public boolean apply(ProgressMsg msg) {
        return msg.getTimestamp() > timestamp;
      }
    });
    return toReturn;
  }
  
  @Override
  public void registerThrottle(ThrottleKey key, Throttle throttle) {
    delegate.registerThrottle(key, throttle);
  }

  @Override
  public <R, P> void execute(Task<R, P> task, P param) {
    delegate.execute(task, param);
  }

  @Override
  public <R, P> void execute(Task<R, P> task, P param, SequentialTaskConfig conf) {
    delegate.execute(task, param, conf);
  }

  @Override
  public <R, P> FutureResult<R> executeAndWait(Task<R, P> task, P param) {
    return delegate.executeAndWait(task, param);
  }

  @Override
  public <R, P> FutureResult<R> executeAndWait(Task<R, P> task, P param, TaskConfig cfg) {
    return delegate.executeAndWait(task, param, cfg);
  }

  @Override
  public <R, P> void executeBackground(Task<R, P> task, P param, BackgroundTaskConfig cfg) {
    delegate.executeBackground(task, param, cfg);
  }

  @Override
  public ProgressQueue getProgressQueue(int level) {
    ProgressQueue queue = new ProgressQueueImpl();
    queues.addProgressQueue(queue, level);
    return queue;
  }

}
