package org.sapia.corus.taskmanager.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * A {@link Throttle} implementation that queues up {@link Runnable}s and
 * executes them one after the other, waiting a specified time interval between
 * each execution.
 * 
 * @author yduchesne
 * 
 */
public class TimeIntervalThrottle implements Throttle {

  /**
   * The time (millis) interval to wait for in between executions.
   */
  private long interval;

  /**
   * The time (millis) at which the last execution occurred.
   */
  private long lastRun;

  /**
   * The {@link BlockingQueue} that is used.
   */
  private BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();

  /**
   * @param unit
   *          the {@link TimeUnit} in which the given time interval is
   *          expressed.
   * @param interval
   *          a time interval.
   */
  public TimeIntervalThrottle(TimeUnit unit, long interval) {
    this.interval = TimeUnit.MILLISECONDS.convert(interval, unit);
    Thread t = new Thread(new ThrottleThread());
    t.setDaemon(true);
    t.start();
  }

  @Override
  public void execute(Runnable toRun) {
    queue.offer(toRun);
  }

  private class ThrottleThread implements Runnable {

    @Override
    public void run() {
      while (true) {
        try {
          Runnable toRun = queue.take();
          if (System.currentTimeMillis() - lastRun < interval) {
            Thread.sleep(interval - (System.currentTimeMillis() - lastRun));
          }
          toRun.run();
          lastRun = System.currentTimeMillis();
        } catch (InterruptedException e) {
          break;
        }
      }
    }
  }

}
