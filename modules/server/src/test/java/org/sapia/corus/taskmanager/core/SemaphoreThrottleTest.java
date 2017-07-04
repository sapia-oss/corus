package org.sapia.corus.taskmanager.core;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;

public class SemaphoreThrottleTest {
  
  private CountDownLatch latch;

  @Before
  public void setUp() throws Exception {
    latch = new CountDownLatch(5);
  }

  @Test
  public void testExecute() throws InterruptedException{
    SemaphoreThrottle throttle = new SemaphoreThrottle(1);
    Runnable toRun = new RunnableImpl();
    long start = System.currentTimeMillis();
    for(int i = 0; i < 5; i++){
      throttle.execute(toRun);
    }
    latch.await();
    long duration = System.currentTimeMillis() - start;
    
    assertTrue("Should have run 1000ms, got " + duration, duration >= 1000);
  }
  
  class RunnableImpl implements Runnable{
    
    @Override
    public void run() {
      try{
        Thread.sleep(200);
        latch.countDown();
      }catch(InterruptedException e){
      }
    }
  }

}
