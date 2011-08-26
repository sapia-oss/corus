package org.sapia.corus.taskmanager.core;

import java.lang.reflect.InvocationTargetException;

/**
 * 
 * @author yduchesne
 *
 */
public class FutureResult<R> {
  
  private volatile boolean completed = false;
  private Object result;

  public synchronized R get() throws InterruptedException, InvocationTargetException{
    return doGet(0);
  }

  public synchronized R get(long timeout) throws InterruptedException, InvocationTargetException{
    return doGet(timeout);
  }

  @SuppressWarnings(value="unchecked")
  private synchronized R doGet(long timeout) throws InterruptedException, InvocationTargetException{
    while(!completed){
      if(timeout <= 0) wait();
      else wait(timeout);
    }
    if(result != null){
      if(result instanceof InvocationTargetException){
        throw (InvocationTargetException)result;
      } 
      else{
        return (R)result;
      }
    }
    else{
      return null;
    }
  }
  
  synchronized void completed(Object result){
    if(result != null && result instanceof Throwable){
      if(result instanceof InvocationTargetException){
        this.result = result;
      }
      else{
        this.result = new InvocationTargetException((Throwable)result);
      }
    }
    else{
      this.result = result;
    }
    completed = true;
    notify();
  }
  
  
}
