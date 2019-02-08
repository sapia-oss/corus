package org.sapia.corus.client;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.sapia.corus.client.Result.Type;
import org.sapia.corus.client.common.Delay;
import org.sapia.ubik.net.ThreadInterruptedException;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Func;
import org.sapia.ubik.util.VoidFunc;

/**
 * An instance of this class aggregates {@link Result} instances.
 * 
 * @author Yanick Duchesne
 */
public class Results<T> implements Iterable<Result<T>> {

  public static final long DEFAULT_INVOCATION_TIMEOUT = 5000;

  private List<Result<T>> results = new ArrayList<Result<T>>();

  private long                    timeout              = DEFAULT_INVOCATION_TIMEOUT;
  private volatile boolean        invocationFinished;
  private volatile int            invocationCount;
  private volatile int            completedCount;
  private List<ResultListener<T>> listeners            = new ArrayList<Results.ResultListener<T>>();

  /**
   * @param listener
   *          a {@link ResultListener}.
   */
  public void addListener(ResultListener<T> listener) {
    listeners.add(listener);
  }

  /**
   * @param timeout
   *          the timeout, in millis, that an instance of this class should wait
   *          for asynchronous results to be available.
   * 
   * @see #hasNext()
   */
  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  /**
   * @param results a new {@link Collection} of {@link Result}s.
   */
  public synchronized void addResults(Collection<Result<T>> results) {
    for (Result<T> r : results) {
      addResult(r);
    }
  }
  
  /**
   * @param result
   *          a {@link Result}.
   */
  public synchronized void addResult(Result<T> result) {
    Assertions.isFalse(result == null, "Cannot add null result");
    results.add(result);
    completedCount++;
    if (completedCount >= invocationCount) {
      invocationFinished = true;
    }
    for (ResultListener<T> listener : listeners) {
      listener.onResult(result);
    }
    notify();
  }

  /**
   * @return <code>true</code> if invocation has completed.
   */
  public boolean isFinished() {
    return invocationFinished;
  }

  /**
   * @return <code>true</code> if this instance contains other objects.
   */
  public synchronized boolean hasNext() {
    if (!invocationFinished) {
      Delay delay = new Delay(timeout, TimeUnit.MILLISECONDS);
      while (results.isEmpty() && delay.isNotOver() && !invocationFinished) {
        try {
          wait(delay.remainingMillisNotZero());
        } catch (InterruptedException e) {
          throw new ThreadInterruptedException();
        }
      }
    }
    return !results.isEmpty();
  }

  /**
   * @return the next object that this instance contains (the returned object is
   *         removed from this instance).
   */
  public Result<T> next() {
    Assertions.illegalState(results.isEmpty(), "No more results");
    return results.remove(0);
  }

  /**
   * increments the internal "invocation count".
   */
  public synchronized void incrementInvocationCount() {
    invocationCount++;
  }

  /**
   * Use for testing purposes only.
   * 
   * @param invocationCount
   *          the invocation count.
   */
  public void setInvocationCount(int invocationCount) {
    this.invocationCount = invocationCount;
  }

  /**
   * decrements the internal "invocation count".
   */
  public synchronized void decrementInvocationCount() {
    invocationCount--;
    if (completedCount >= invocationCount) {
      invocationFinished = true;
      notifyAll();
    }
  }
  
  /**
   * @return flattens the element in each result into a single list.
   */
  @SuppressWarnings("rawtypes")
  public List<Object> flatten() {
    List<Object> toReturn = new ArrayList<>();
    while (hasNext()) {
      Result<T> result = next();
      if (result.getType() == Type.COLLECTION) {
        for (Object o : ((Collection) result.getData())) {
          toReturn.add(o);
        }
      } else  {
        toReturn.add(result.getData());
      }
    }
    return toReturn;
  }

  @Override
  public Iterator<Result<T>> iterator() {
    return new ResultIterator();
  }

  /**
   * @param filter the filter {@link Func} to use.
   * @param errorHandler the handler {@link VoidFunc} of error 
   * @return the filtered {@link Results}.
   */
  public Results<T> filter(Func<T, T> filter, VoidFunc<Throwable> errorHandler) {
    Results<T> filtered = new Results<T>();
    while (hasNext()) {
      try {
        Result<T> result = next();
        filtered.addResult(new Result<T>(result.getOrigin(), filter.call(result.getData()), result.getType()));
      } catch (UndeclaredThrowableException ute) {
        if (ute.getCause() != null) {
          errorHandler.call(ute.getCause());
        } else {
          errorHandler.call(ute);
        }
      } catch (Exception e) {
        errorHandler.call(e);
      }
    }
    filtered.invocationFinished = true;
    return filtered;
  }

  // ==========================================================================
  // Inner classes

  /**
   * An instance of this interface can be added to a {@link Results} instance in
   * order to be notified of new {@link Result}s.
   */
  public interface ResultListener<T> {

    /**
     * @param result
     *          a {@link Result} that has been received.
     */
    public void onResult(Result<T> result);
  }

  // --------------------------------------------------------------------------

  class ResultIterator implements Iterator<Result<T>> {
    @Override
    public boolean hasNext() {
      return Results.this.hasNext();
    }

    @Override
    public Result<T> next() {
      return Results.this.next();
    }

    @Override
    public void remove() {
    }
  }
}
