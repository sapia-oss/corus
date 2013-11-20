package org.sapia.corus.taskmanager.core;

/**
 * This interface specifies task execution behavior.
 * 
 * @author yduchesne
 */
public interface TaskManager {

  /**
   * Executes the given task in a separate thread. The calling wait returns as
   * soon as the task is submitted - and not necessarily executed..
   * 
   * @param task
   *          the {@link Task} to execute.
   * @param param
   *          an arbitrary parameter to pass to the task.
   */
  public <R, P> void execute(Task<R, P> task, P param);

  /**
   * @param task
   *          the {@link Task} to execute.
   * @param param
   *          an arbitrary parameter to pass to the task.
   * @param conf
   *          a {@link SequentialTaskConfig}
   */
  public <R, P> void execute(Task<R, P> task, P param, SequentialTaskConfig conf);

  /**
   * Executes the given task sequentially returning a {@link FutureResult} which
   * can be blocked upon.
   * 
   * @param task
   *          the {@link Task} to execute
   * @return a {@link FutureResult} on which the calling thread can block.
   * @param param
   *          an arbitrary parameter to pass to the task.
   */
  public <R, P> FutureResult<R> executeAndWait(Task<R, P> task, P param);

  /**
   * This method takes a task log that will be used by the given task to log its
   * activity, upon execution.
   * 
   * @param task
   *          a {@link Task}
   * @param param
   *          an arbitrary parameter to pass to the task.
   * @param conf
   *          a {@link TaskConfig}
   * @see #executeAndWait(Task)
   */
  public <R, P> FutureResult<R> executeAndWait(Task<R, P> task, P param, TaskConfig conf);

  /**
   * Executes the given task in the background, indefinitely.
   * 
   * @param task
   *          the {@link Task} to execute.
   * @param param
   *          an arbitrary parameter to pass to the task.
   * @param config
   *          a {@link BackgroundTaskConfig}.
   */
  public <R, P> void executeBackground(Task<R, P> task, P param, BackgroundTaskConfig config);

  /**
   * Registers a {@link ThrottleKey} to a {@link Throttle}.
   * 
   * @param key
   *           a {@link ThrottleKey}
   * @param throttle
   *          a {@link Throttle}
   */
  public void registerThrottle(ThrottleKey key, Throttle throttle);
}
