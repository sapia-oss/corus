package org.sapia.corus.taskmanager.core;


public interface TaskManager {

  /**
   * Executes the given task in a separate thread. The calling wait returns
   * as soon as the task is submitted - and not necessarily executed..
   * 
   * @param task the {@link Task} to execute.
   */
  public void execute(Task task);
  
  /**
   * @param task the {@link Task} to execute.
   * @param conf a {@link SequentialTaskConfig}
   */
  public void execute(Task task, SequentialTaskConfig conf);
  
  /**
   * Executes the given task sequentially returning a {@link FutureResult} 
   * which can be blocked upon.
   * 
   * @param task the {@link Task} to execute
   * @return a {@link FutureResult} on which the calling thread can block.
   */
  public FutureResult executeAndWait(Task task);
  
  /**
   * This method takes a task log that will be used by the given
   * task to log its activity, upon execution.
   * 
   * @param task a {@link Task}
   * @param conf a {@link TaskConfig}
   * @see #executeAndWait(Task)
   */
  public FutureResult executeAndWait(Task task, TaskConfig conf);
 
  /**
   * Executes the given task in the background, indefinitely.
   * @param task the {@link Task} to execute.
   * @param config a {@link BackgroundTaskConfig}.
   */
  public void executeBackground(Task task, BackgroundTaskConfig config);
}
