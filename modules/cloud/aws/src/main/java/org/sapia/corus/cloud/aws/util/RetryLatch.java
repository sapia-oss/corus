package org.sapia.corus.cloud.aws.util;

/**
 * Keeps the state of a given operation being retried.
 * 
 * @author yduchesne
 *
 */
public class RetryLatch {
  
  /**
   * An instance of this interface is registered with a {@link RetryLatch}, and is notified when retries occur.
   * Actuall called from {@link RetryLatch#increment()}, after the attempt counter has been incremented.
   * 
   * @author yduchesne
   *
   */
  public interface RetryListener {
    
    /**
     * @param currentNumberOfAttempts the current number of attempts.
     */
    public void onRetry(int currentNumberOfAttempts);
  }
  
  //  =========================================================================
  
  private RetryCriteria criteria;
  private int           attempts;
  private TimeMeasure   startTime;
  private RetryListener listener = new RetryListener() {
    @Override
    public void onRetry(int currentNumberOfAttempts) {
      // noop
    }
  };
  
  public RetryLatch(RetryCriteria criteria) {
    this.criteria  = criteria;
    this.startTime = TimeMeasure.forCurrentTime(criteria.getRetryInterval().getSupplier());
  }
  
  /**
   * @return this instance, for a chained invocation.
   * 
   * @see RetryCriteria
   */
  public RetryLatch increment() {
    attempts++;
    if (listener != null) {
      listener.onRetry(attempts);
    }
    return this;
  }
  
  /**
   * @return <code>true</code> if the maximum number of attempts has been reached.
   */
  public boolean shouldStop() {
    return criteria.isOver(attempts, startTime);
  }
  
  /**
   * @return <code>true</code> if the maximum number of attempts has not been reached.
   */
  public boolean shouldContinue() {
    return !shouldStop();
  }

  /**
   * Increments the attempt counter, and then internally calls {@link #shouldStop()} to check
   * if the maximum number of attempts has been reached.
   * <p>
   * If that isn't the case: pauses for the duration specified to this instance at 
   * construction time.
   *
   * @return this instance, for a chained invocation.
   * @throws InterruptedException if the calling thread is interrupted while pausing.
   */
  public RetryLatch incrementAndPause() throws InterruptedException {
    increment();
    if (!shouldStop()) {
      pause();
    }
    return this;
  }
  /**
   * Internally calls {@link RetryCriteria#pause()}. 
   * 
   * @throws InterruptedException if the calling thread is interrupted.
   */
  public void pause() throws InterruptedException {
    criteria.pause();
  }

  /**
   * @param listener the {@link RetryListener} to notify.
   * @return this instance.
   */
  public RetryLatch withRetryListener(RetryListener listener) {
    this.listener = listener;
    return this;
  }
}
