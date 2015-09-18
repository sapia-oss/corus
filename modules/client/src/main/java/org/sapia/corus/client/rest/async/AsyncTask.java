package org.sapia.corus.client.rest.async;

/**
 * Specifies the behavior common to asynchronous tasks.
 * 
 * @author yduchesne
 *
 */
public interface AsyncTask {
  
  /**
   * Executes this instance.
   */
  public void execute();

  /**
   * Forces the termination of this task, if it is running.
   */
  public void terminate();
  
  /**
   * Called systematically either after a task has been executed, or forcefully terminated.
   */
  public void releaseResources();
  
  /**
   * @return <code>true</code> if this task is currently running.
   */
  public boolean isRunning();

}