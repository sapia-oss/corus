package org.sapia.corus.http.rest;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.corus.client.rest.async.AsyncTask;
import org.sapia.corus.client.rest.async.AsynchronousCompletionService;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.SysClock;
import org.sapia.ubik.util.SysClock.RealtimeClock;
import org.sapia.ubik.util.TimeValue;

/**
 * Implements the {@link AsynchronousCompletionService} interface.
 * 
 * @author yduchesne
 *
 */
public class AsynchronousCompletionServiceImpl implements AsynchronousCompletionService {
  
  /**
   * Abstracts the mechanism used for asynchronous task execution - eases unit testing.
   * 
   * @author yduchesne
   *
   */
  public interface TaskExecutionProvider {
  
    /**
     * @param task an {@link AsyncTask} whose execution should be scheduled.
     */
    public void scheduleForExecution(AsyncTask task);
  
  }
  
  // ==========================================================================
  
  private Logger                        log          = Hierarchy.getDefaultHierarchy().getLoggerFor(getClass().getName());
  private SysClock                      clock        = RealtimeClock.getInstance();
  private Map<String, AsyncTaskWrapper> tasksByToken;
  private TaskExecutionProvider         executor;
  private TimeValue                     taskTimeout;
  
  /**
   * @param internalMap a {@link Map} implementation to use for keeping {@link AsyncTask}s.
   * @param executor the {@link TaskExecutionProvider} to use for actual task execution.
   * @param taskTimeout the {@link TimeValue} corresponding to the maximum amount of time an {@link AsyncTask}
   * can be kept without the submitting party accessing it (using {@link #getAsyncTask(String, Class)}).
   */
  public AsynchronousCompletionServiceImpl(
      Map<String, AsyncTaskWrapper> internalMap, 
      TaskExecutionProvider         executor,
      TimeValue                     taskTimeout) {
    this.tasksByToken = internalMap;
    this.executor     = executor;
    this.taskTimeout  = taskTimeout;
  }

  /**
   * @param executor the {@link TaskExecutionProvider} to use for actual task execution.
   * @param taskTimeout the {@link TimeValue} corresponding to the maximum amount of time an {@link AsyncTask}
   * can be kept without the submitting party accessing it (using {@link #getAsyncTask(String, Class)}).
   */
  public AsynchronousCompletionServiceImpl(
      TaskExecutionProvider         executor,
      TimeValue                     taskTimeout) {
    this(new ConcurrentHashMap<String, AsyncTaskWrapper>(), executor, taskTimeout);
   }
  
  /**
   * @param clock the {@link SysClock} to use.
   */
  void setClock(SysClock clock) {
    this.clock = clock;
  }
  
  @Override
  public String registerForExecution(AsyncTask task) {
    String completionToken = UUID.randomUUID().toString();
    if (log.isDebugEnabled()) log.debug(String.format("Registering task for execution: %s (completion token = %s)", task, completionToken));
    tasksByToken.put(completionToken, new AsyncTaskWrapper(task));
    try {
      executor.scheduleForExecution(task);
    } catch (RuntimeException e) {
      log.error(String.format("Could not register task for execution: %s (completion token = %s)", task, completionToken), e);
      tasksByToken.remove(completionToken);
      throw e;
    }
    return completionToken;
  }
  
  @Override
  public <T extends AsyncTask> T getAsyncTask(String completionToken, Class<T> taskType) throws IllegalArgumentException {
    AsyncTaskWrapper wrapper = tasksByToken.get(completionToken);
    Assertions.notNull(wrapper, "No asynchronous task currently running associated to token: %s", completionToken);
    wrapper.touch();
    return taskType.cast(wrapper.getTask());
  }
  
  @Override
  public void unregister(String completionToken) {
    if (log.isDebugEnabled()) log.debug("Unregistering task for completion token: " + completionToken);
    tasksByToken.remove(completionToken);
  }
  
  @Override
  public void shutdown() {
    for (Map.Entry<String, AsyncTaskWrapper> t : tasksByToken.entrySet()) {
      t.getValue().task.terminate();
      t.getValue().task.releaseResources();
    }
  }
  
  /**
   * Flushes the tasks that haven't been access for the amount specified by this instances's 
   * configured task timeout.
   */
  public void flushStaleTasks() {
    for (String token : tasksByToken.keySet()) {
      AsyncTaskWrapper taskWrapper = tasksByToken.get(token);
      if (taskWrapper.isStale()) {
        if (log.isDebugEnabled()) log.debug(String.format("Task %s (completion token = %s) is stale", taskWrapper.task, token));
        if (taskWrapper.task.isRunning()) {
          taskWrapper.task.terminate();
        }
        taskWrapper.task.releaseResources();
        tasksByToken.remove(token);
      }
    }
  }

  // ==========================================================================
 
  /**
   * Internal wrapper class: an instance wraps an {@link AsyncTask}, and its last access time.
   * @author yduchesne
   *
   */
  class AsyncTaskWrapper {
    
    private volatile long lastAccessTime = clock.currentTimeMillis();
    
    private AsyncTask task;
    
    AsyncTaskWrapper(AsyncTask task) {
      this.task = task;
    }
    
    AsyncTask getTask() {
      return task;
    }
    
    boolean isStale() {
      return clock.currentTimeMillis() - lastAccessTime > taskTimeout.getValueInMillis();
    }
    
    AsyncTaskWrapper touch() {
      lastAccessTime = clock.currentTimeMillis();
      return this;
    }
  }

}
