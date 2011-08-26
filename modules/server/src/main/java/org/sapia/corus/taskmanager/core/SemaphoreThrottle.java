package org.sapia.corus.taskmanager.core;

import java.util.concurrent.Semaphore;

/**
 * A {@link Throttle} that is based on a {@link Semaphore}. An instance of this class 
 * will tolerate up to a certain amount of concurrent executions. 
 * 
 * @author yduchesne
 *
 */
public class SemaphoreThrottle implements Throttle{

  /**
   * The {@link Semaphore} that this instance uses for synchronization.
   */
  private Semaphore semaphore;
  
  /**
   * @param maxConcurrent the maximum number of concurrent instances.
   */
  public SemaphoreThrottle(int maxConcurrent) {
    semaphore = new Semaphore(maxConcurrent);
  }
  
  @Override
  public void execute(Runnable toRun) {
    try{
      semaphore.acquire();
    }catch(InterruptedException e){
      return;
    }
    
    try{
      toRun.run();
    }finally{
      semaphore.release();
    }
  }

}
