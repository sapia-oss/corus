package org.sapia.corus.client.rest.async;


/**
 * Support class for implemeting {@link AsyncTask} instances.
 * 
 * @author yduchesne
 *
 */
public abstract class AsyncTaskSupport implements AsyncTask {
  
  private volatile boolean isRunning;

  /**
   * Sets this instance's {@link #isRunning()} flag to <code>true</code> and 
   * internally invokes {@link #doExecute()}.
   */
  @Override
  public void execute() {
    isRunning = true;
    try {
      doExecute();
    } finally {
      isRunning = false;
    }
  }
  
  /**
   * Internally invokes {@link #doTerminate()} and then set's this instance's 
   * {@link #isRunning()} flag to <code>false</code>, in a guarded block.
   */
  @Override
  public void terminate() {
    try {
      doTerminate();
    } finally {
      isRunning = false;
    }
  }
  
  @Override
  public boolean isRunning() {
    return isRunning;
  }
  
  /**
   * Template method to be overridden by inheriting classes.
   */
  protected abstract void doTerminate();

  /**
   * Template method to be overridden by inheriting classes.
   */
  protected abstract void doExecute();

}
