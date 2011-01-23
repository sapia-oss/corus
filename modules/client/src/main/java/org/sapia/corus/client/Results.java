package org.sapia.corus.client;

import java.util.ArrayList;
import java.util.List;

/**
 * An instance of this class aggregates {@link Result} instances.
 * 
 * @author Yanick Duchesne
 */
public class Results<T> {
  private List<Result<T>>    _results = new ArrayList<Result<T>>();
  private boolean _invocationFinished;
  private int _invocationCount;
  private int _completedCount;
  private long _timeout = 5000;
  
  /**
   * @param timeout the timeout, in millis, that an instance of this class should wait 
   * for asynchronous results to be available.
   * 
   * @see #hasNext()
   */
  public void setTimeout(long timeout){
    _timeout = timeout;
  }
  
  /**
   * @param result a {@link Result}.
   */
  public synchronized void addResult(Result<T> result) {
    _results.add(result);
    _completedCount++;
    if(_completedCount >= _invocationCount){
      _invocationFinished = true;
    }
    notify();
  }
  
  public boolean isFinished(){
    return _invocationFinished;
  }

  /**
   * @return <code>true</code> if this instance contains other objects.
   */
  public synchronized boolean hasNext() {
    while (_results.size() == 0) {
      if(_invocationFinished){
        return false;
      }
      long start = System.currentTimeMillis();
      try {
        wait(_timeout);
      } catch (InterruptedException e) {
        return false;
      }
      
      if(_results.size() == 0){
        if(_invocationFinished ||
           // is timed out ?
           (System.currentTimeMillis() - start > _timeout)){
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
    return _results.size() > 0;
  }

  /**
   * @return the next object that this instance contains (the returned object
   * is removed from this instance).
   */
  public Result<T> next() {
    return _results.remove(0);
  }
  
  /**
   * increments the internal "invocation count".
   */
  public synchronized void incrementInvocationCount(){
    _invocationCount++;
  }
  
  /**
   * decrements the internal "invocation count".
   */
  public synchronized void decrementInvocationCount(){
    _invocationCount--;
    if(_completedCount >= _invocationCount){
      _invocationFinished = true;
      notifyAll();
    }
  }
}
