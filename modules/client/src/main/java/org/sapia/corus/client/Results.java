package org.sapia.corus.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sapia.ubik.util.Function;

/**
 * An instance of this class aggregates {@link Result} instances.
 * 
 * @author Yanick Duchesne
 */
public class Results<T> implements Iterable<Result<T>> {
  
	public static final long DEFAULT_INVOCATION_TIMEOUT = 5000;
	
  private List<Result<T>> results 						= new ArrayList<Result<T>>();
  
  private long 						timeout 						= DEFAULT_INVOCATION_TIMEOUT;
  private boolean 			  invocationFinished;
  private int 					  invocationCount;
  private int 					  completedCount;
  private List<ResultListener<T>> listeners = new ArrayList<Results.ResultListener<T>>();
 
  /**
   * @param listener a {@link ResultListener}.
   */
  public void addListener(ResultListener<T> listener) {
    listeners.add(listener);
  }
  
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
    for (ResultListener<T> listener : listeners) {
      listener.onResult(result);
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
    if (!invocationFinished) {
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
  public synchronized void incrementInvocationCount() {
    invocationCount++;
  }
  
  /**
   * Use for testing purposes only.
   * 
   * @param invocationCount the invocation count.
   */
  public void setInvocationCount(int invocationCount) {
    this.invocationCount = invocationCount;
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
  
  @Override
  public Iterator<Result<T>> iterator() {
    return new ResultIterator();
  }
  
  /**
   * @param filter the filter {@link Function} to use.
   * @return the filtered {@link Results}.
   */
  public Results<T> filter(Function<T, T> filter) {
    Results<T> filtered = new Results<T>();
    while (hasNext()) {
      Result<T> result = next();
      filtered.addResult(new Result<T>(result.getOrigin(), filter.call(result.getData())));
    }
    filtered.invocationFinished = true;
    return filtered;
  }
   
  // ==========================================================================
  // Inner classes
  
  /**
   * An instance of this interface can be added to a {@link Results} instance in order to be notified
   * of new {@link Result}s.
   */
  public interface ResultListener<T> {
    
    /**
     * @param result a {@link Result} that has been received.
     */
    public void onResult(Result<T> result);
  }
  
  // --------------------------------------------------------------------------
  
  class ResultIterator implements Iterator<Result<T>> {
    @Override
    public boolean hasNext() {
      return Results.this.hasNext();
    }
    
    @Override
    public Result<T> next() {
      return Results.this.next();
    }
    
    @Override
    public void remove() {
    }
  }
 }
