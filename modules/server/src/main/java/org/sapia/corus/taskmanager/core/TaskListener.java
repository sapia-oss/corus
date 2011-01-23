package org.sapia.corus.taskmanager.core;


/**
 * Allows registering a callback with a {@link TaskManager} in order to be
 * notified when execution of a given task has completed.
 * 
 * @author yduchesne
 *
 */
public interface TaskListener {
 
  /**
   * Call when execution of a given task completes successfully.
   * @param task the {@link Task} that has completed.
   * @param result the actual result of the execution, or <code>null</code>
   * if the task returned no result.
   * @see Task#execute(TaskExecutionContext)
   */
  public void executionSucceeded(Task task, Object result);
 
  /**
   * Call when execution of a given task result in an error .
   * @param task the {@link Task} that has completed.
   * @param err the actual error that was thrown.
   * @see Task#execute(TaskExecutionContext)
   */
  public void executionFailed(Task task, Throwable err);
}
