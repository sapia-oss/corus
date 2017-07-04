package org.sapia.corus.taskmanager.core;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class TimeIntervalThrottleTest {


  @Test
  public void testExecuteVerifyNumberOfTimes() throws Exception{
    TimeIntervalThrottle throttle = new TimeIntervalThrottle(TimeUnit.MILLISECONDS, 200);
    Runnable toRun = mock(Runnable.class);
    for(int i = 0; i < 5; i++){
      throttle.execute(toRun);
    }
    Thread.sleep(1200);
    verify(toRun, times(5)).run();
  }
  
  @Test
  public void testExecuteVerifyCumulativeTime() throws Exception{
    TimeIntervalThrottle throttle = new TimeIntervalThrottle(TimeUnit.MILLISECONDS, 200);
    RunnableImpl toRun = new RunnableImpl();
    for(int i = 0; i < 5; i++){
      throttle.execute(toRun);
    }
    Thread.sleep(1000);
    
    // 0, 200, 400, 600, 700
    assertTrue("Should have taken at least 1000ms, got " + toRun.duration(), toRun.duration() >= (4 * 200));
  }  
  
  class RunnableImpl implements Runnable{
    private long start;
    private long end;
    
    long duration(){
      return end - start;
    }
    
    @Override
    public void run() {
      if(start == 0) start = System.currentTimeMillis();
      end = System.currentTimeMillis();
    }
  }

}
