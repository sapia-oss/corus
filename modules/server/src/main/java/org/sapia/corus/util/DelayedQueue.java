package org.sapia.corus.util;

import java.util.List;

import org.sapia.ubik.util.Func;
import org.sapia.ubik.util.Pause;
import org.sapia.ubik.util.SysClock;
import org.sapia.ubik.util.TimeValue;

public class DelayedQueue<T> extends Queue<T> {
  
  private SysClock      clock;
  private Sleeper       sleeper;
  private long          inactivityDelay;
  private volatile long lastActivityTime;
  private long          checkIntervalTime;
  
  public DelayedQueue(SysClock clock, Sleeper sleeper, TimeValue inactivityDelay, TimeValue checkIntervalTime) {
    this.clock             = clock;
    this.sleeper           = sleeper;
    this.inactivityDelay   = inactivityDelay.getValueInMillis();
    this.checkIntervalTime = checkIntervalTime.getValueInMillis();
  }
  
  public DelayedQueue(TimeValue inactivityDelay, TimeValue checkIntervalTime) {
    this(SysClock.RealtimeClock.getInstance(), Sleeper.ThreadSleeper.getInstance(), inactivityDelay, checkIntervalTime);
  }
  
  @Override
  public synchronized Queue<T> add(T...item) {
    lastActivityTime = clock.currentTimeMillis();
    return super.add(item);
  }
  
  public List<T> removeAllAfterInactivity(long timeout) throws InterruptedException {
    Pause pause = new Pause(clock, timeout);
    while (!pause.isOver()) {
      if (clock.currentTimeMillis() - lastActivityTime >= inactivityDelay) {
        lastActivityTime = 0;
        break;
      } else {
        sleeper.sleep(checkIntervalTime);
      }
    }   
    return super.removeAll();
  }
  
  public T removeFirstAfterInactivity(long timeout) throws InterruptedException {
    return doRemove(
        new Func<T, Void>() {
           @Override
          public T call(Void arg) {
            return removeFirst();
          }
      
        }, timeout
    );
  }
  
  public T removeLastAfterInactivity(long timeout) throws InterruptedException {
    return doRemove(
        new Func<T, Void>() {
           @Override
          public T call(Void arg) {
            return removeLast();
          }
      
        }, timeout
    );
  }
  
  private T doRemove(Func<T, Void> func, long timeout) throws InterruptedException {
    Pause pause = new Pause(clock, timeout);
    while (!pause.isOver()) {
      if (clock.currentTimeMillis() - lastActivityTime >= inactivityDelay) {
        lastActivityTime = 0;
        break;
      } else {
        sleeper.sleep(checkIntervalTime);
      }
    }   
    return func.call(null);
  }
  
}
