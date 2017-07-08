package org.sapia.corus.ftest.rest.stress;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.sapia.ubik.concurrent.ConfigurableExecutor;
import org.sapia.ubik.concurrent.ConfigurableExecutor.ThreadingConfiguration;
import org.sapia.ubik.util.TimeValue;

import com.google.common.base.Stopwatch;

/**
 * Runs {@link StressTestTask}s concurrently, using a given number of thread.
 * 
 * @author yduchesne
 *
 */
public class StressTestTaskRunner {
  
  private ExecutorService executor;
  private int             nThreads;
  
  /**
   * @param nThreads the number of threads to use.

   */
  public StressTestTaskRunner(int nThreads) {
    executor = new ConfigurableExecutor(
      ThreadingConfiguration.newInstance()
        .setCorePoolSize(nThreads)
        .setMaxPoolSize(nThreads)
        .setQueueSize(nThreads)
    );
    this.nThreads = nThreads;
  }
  
  /**
   * Runs provided {@link StressTestTask}s, each within its own thread.
   * 
   * @param taskSupplier the {@link Supplier} of {@link StressTestTask} which will
   *                     be submitted for execution.
   * @throws Exception if an error occurs while any of the task's execution.
   */
  public StressTestStatus run(Supplier<StressTestTask> taskSupplier) throws Exception {
    Stopwatch sw = Stopwatch.createStarted();
    List<Future<?>>      results = new ArrayList<>(nThreads);
    List<StressTestTask> tasks   = new ArrayList<>(nThreads);
    for (int i = 0; i < nThreads; i++) {
      StressTestTask task = taskSupplier.get();
      tasks.add(task);
      Future<?> result = executor.submit(task);
      results.add(result);
    }
    
    for (Future<?> result : results) {
      result.get();
    }
    
    int totalRunCount = 0;
    for (StressTestTask t : tasks) {
      totalRunCount += t.getTotalRunCount();
    }
    
    return new StressTestStatus(TimeValue.createMillis(sw.elapsed(TimeUnit.MILLISECONDS)), totalRunCount); 
    
  }
  
  /**
   * Internally shuts terminates the task that are running - if any is still running.
   * Tasks that are paused will be interrupted.
   * 
   * @see Thread#interrupt()
   */
  public void shutdown() {
    executor.shutdownNow();
  }

}
