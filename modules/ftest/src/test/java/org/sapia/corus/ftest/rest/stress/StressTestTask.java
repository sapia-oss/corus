package org.sapia.corus.ftest.rest.stress;

import java.util.concurrent.TimeUnit;

import org.sapia.corus.ftest.FtestClient;
import org.sapia.ubik.util.TimeValue;

import com.google.common.base.Stopwatch;

/**
 * Abstract class to be inherited by classes that are meant to run stress tests
 * against Corus, using a {@link FtestClient}.
 * 
 * @author yduchesne
 *
 */
public abstract class StressTestTask implements Runnable {
  
  private int  runCount;
  private long pauseBetweenRunsMillis;
  private int  totalRunCount;
  
  /**
   * @param runCount         the number of runs to do.
   * @param pauseBetweenRuns a {@link TimeValue} specifying the amount of time to wait for between runs.
   */
  protected StressTestTask(int runCount, TimeValue pauseBetweenRuns) {
    this(runCount, pauseBetweenRuns.getValueInMillis());
  }

  /**
   * @param runCount         the number of runs to do.
   * @param pauseBetweenRuns the amount of time to wait for between runs.
   */
  protected StressTestTask(int runCount, long pauseBetweenRunsMillis) {
    this.runCount               = runCount;
    this.pauseBetweenRunsMillis = pauseBetweenRunsMillis;
  }
  
  /**
   * @return the total number of runs.
   */
  public int getTotalRunCount() {
    return totalRunCount;
  }
  
  @Override
  public void run() {
    FtestClient client = FtestClient.open();
    try {
      for(int i = 0; i < runCount; i++) {
        Stopwatch chrono = Stopwatch.createStarted();
        doRun(client);
        chrono.stop();
        System.out.println(
          String.format("%s >> Completed run #%s/%s in: %s", 
            Thread.currentThread().getName(),
            i + 1,
            runCount,
            chrono.elapsed(TimeUnit.MILLISECONDS)
          )
        );
        totalRunCount++;
        if (pauseBetweenRunsMillis > 0) {
          Thread.sleep(pauseBetweenRunsMillis);
        }
      }
    } catch (Exception e) {
      throw new IllegalStateException("Error running stress test task", e);  
    } finally {
      client.close();
    }
    
  }
  
  /**
   * @param client the {@link FtestClient} to use.
   * 
   * @throws Exception if an error occurs while executing this method.
   */
  protected abstract void doRun(FtestClient client) throws Exception;

}
