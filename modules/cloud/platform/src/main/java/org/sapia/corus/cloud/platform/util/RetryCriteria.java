package org.sapia.corus.cloud.platform.util;

/**
 * Encapsulates data defining so-called retry criteria: how many times an operation should be retried upon failure, and the amount
 * of time to wait between each attempt.
 * 
 * @author yduchesne
 *
 */
public class RetryCriteria {

  private TimeMeasure  retryInterval, maxRetryDuration;
  private int          maxAttempts;
  private Sleeper      sleeper = Sleeper.SystemSleeper.getInstance();
  
  public RetryCriteria(TimeMeasure retryInterval, int maxAttempts) {
    this.retryInterval         = retryInterval;
    this.maxAttempts           = maxAttempts;
    this.maxRetryDuration      = TimeMeasure.forMillis(maxAttempts * retryInterval.getMillis()).withTimeSupplier(retryInterval.getSupplier());
  }
  
  public RetryCriteria(TimeMeasure retryInterval, TimeMeasure maxRetryDuration) {
    this.retryInterval         = retryInterval;
    this.maxAttempts           = (int) (maxRetryDuration.getMillis() / retryInterval.getMillis());
    this.maxRetryDuration      = maxRetryDuration;
  }
  
  public long getMaxAttempts() {
    return maxAttempts;
  }
  
  /**
   * @return the {@link TimeMeasure} corresponding to this instance's retry interval.
   */
  public TimeMeasure getRetryInterval() {
    return retryInterval;
  }
  
  /**
   * Internally makes the calling thread sleep, according to this instance's specified interval.
   * 
   * @throws InterruptedException if the calling thread is interrupted while pausing.
   */
  public void pause() throws InterruptedException {
    sleeper.sleep(retryInterval.getMillis());
  }
  
  /**
   * @param currentNumberOfAttempts the current number of attempts.
   * @param startTime the {@link TimeMeasure} corresponding to the time at which the measure operation started.
   * @return <code>true</code> if the measured operation's retry should be aborted.
   */
  public boolean isOver(int currentNumberOfAttempts, TimeMeasure startTime) {
    if (currentNumberOfAttempts >= maxAttempts) {
      return true;
    } else if (maxRetryDuration != null) {
      return maxRetryDuration.getSupplier().currentTimeMillis() - startTime.getMillis() >= maxRetryDuration.getMillis();
    }
    return false;
  }
  
  /**
   * @param sleeper a {@link Sleeper} to use.
   * @return a copy of this instance, but encapsulating the given {@link Sleeper}.
   */
  public RetryCriteria withSleeper(Sleeper sleeper) {
    RetryCriteria copy = new RetryCriteria(retryInterval, maxRetryDuration);
    copy.sleeper = sleeper;
    copy.maxAttempts = maxAttempts;
    return copy;
  }
  
  // --------------------------------------------------------------------------
  // Factory methods
  
  public static RetryCriteria forMaxDuration(TimeMeasure retryInterval, TimeMeasure maxDuration) {
    return new RetryCriteria(retryInterval, maxDuration);
  }
  
  public static RetryCriteria forMaxAttempts(TimeMeasure retryInterval, int maxAttempts) {
    return new RetryCriteria(retryInterval, maxAttempts);
  }
}
