package org.sapia.corus.client.rest.async;

import java.util.concurrent.TimeoutException;

import org.sapia.corus.client.rest.ProgressResult;

/**
 * To be implemented by tasks that return {@link ProgressResult} data.
 * 
 * @author yduchesne
 *
 */
public interface ProgressCapableTask extends AsyncTask {

  /**
   * @return the next {@link ProgressResult}.
   * 
   * @throws TimeoutException if this task's timeout has been reached.
   */
  public ProgressResult getNextResult() throws TimeoutException;
  
  /**
   * @return a {@link ProgressResult} resulting from the aggregation of all {@link ProgressResult}s
   * instances internally accumulated thus for.
   */
  public ProgressResult drainAllResults();

}
