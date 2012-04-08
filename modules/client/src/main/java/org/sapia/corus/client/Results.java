package org.sapia.corus.client;

import java.util.ArrayList;
import java.util.List;

/**
 * An instance of this class aggregates {@link Result} instances.
 * 
 * @author Yanick Duchesne
 */
public class Results<T> {
	
	public static final long DEFAULT_INVOCATION_TIMEOUT = 5000;
	
  private List<Result<T>> results 						= new ArrayList<Result<T>>();
  private long 						timeout 						= DEFAULT_INVOCATION_TIMEOUT;
  private boolean 			  invocationFinished;
  private int 					  invocationCount;
  private int 					  completedCount;
  
  /**
   * @param timeout the timeout, in millis, that an instance of this class should wait 
   * for asynchronous results to be available.
   * 
   * @see #hasNext()
   */
  public void setTimeout(long timeout){
    this.timeout = timeout;
  }
  
  /**
   * @param result a {@link Result}.
   */
  public synchronized void addResult(Result<T> result) {
    results.add(result);
    completedCount++;
    if(completedCount >= invocationCount){
      invocationFinished = true;
    }
    notify();
  }
  
  /**
   * @return <code>true</code> if invocation has completed.
   */
  public boolean isFinished(){
    return invocationFinished;
  }

  /**
   * @return <code>true</code> if this instance contains other objects.
   */
  public synchronized boolean hasNext() {
    while (results.size() == 0) {
      if(invocationFinished){
        return false;
      }
      long start = System.currentTimeMillis();
      try {
        wait(timeout);
      } catch (InterruptedException e) {
        return false;
      }
      
      if(results.size() == 0){
        if(invocationFinished ||
           // is timed out ?
           (System.currentTimeMillis() - start > timeout)){
          break;
        }
        else{
          continue;
        }
      }
      else{
        break;
      }      
    }
    return results.size() > 0;
  }

  /**
   * @return the next object that this instance contains (the returned object
   * is removed from this instance).
   */
  public Result<T> next() {
    return results.remove(0);
  }
  
  /**
   * increments the internal "invocation count".
   */
  public synchronized void incrementInvocationCount(){
    invocationCount++;
  }
  
  /**
   * decrements the internal "invocation count".
   */
  public synchronized void decrementInvocationCount(){
    invocationCount--;
    if(completedCount >= invocationCount){
      invocationFinished = true;
      notifyAll();
    }
  }
}
