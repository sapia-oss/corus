package org.sapia.corus.client.rest.async;


/**
 * Interface that allows executing some REST calls asynchronously.
 * 
 * @author yduchesne
 *
 */
public interface AsynchronousCompletionService {

  /**
   * @param task an {@link AsyncTask} instance.
   * 
   * @return the completion token to use to request for status.
   */
  public String registerForExecution(AsyncTask task);
  
  /**
   * @param completionToken the completion token with which the expected async task was registered.
   * @param taskType the concrete class of the {@link AsyncTask} to return.
   * @return the {@link AsyncTask} corresponding to the given completion token.
   * @throws IllegalArgumentException if no async task could be found for the given completion token.
    */
  public <T extends AsyncTask> T getAsyncTask(String completionToken, Class<T> taskType) throws IllegalArgumentException;
  
  /**
   * Unregisters the {@link AsyncTask} corresponding to the given token. Internally calls {@link AsyncTask#terminate()} on the
   * task, to insure proper clean up of resources it may hold.
   * 
   * @param completionToken a completion token, corresponding to a currently registered task.
   */
  public void unregister(String completionToken);
  
  /**
   * Shuts down this instance (terminates all currently running tasks).
   */
  public void shutdown();
}
