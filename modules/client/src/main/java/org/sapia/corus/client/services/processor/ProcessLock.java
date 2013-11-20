package org.sapia.corus.client.services.processor;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import org.sapia.corus.client.common.Delay;
import org.sapia.corus.client.exceptions.processor.ProcessLockException;

/**
 * Models a lock on a {@link Process}.
 * 
 * @author yduchesne
 * 
 */
public class ProcessLock implements Serializable {

  static final long serialVersionUID = 1L;

  private transient LockOwner lockOwner;

  /**
   * Acquires the lock on this instance.
   * 
   * @param leaser
   *          the object that attempts to obtain the lock on this instance.
   * @throws ProcessLockException
   *           if this instance is already locked by another object.
   */
  public synchronized void acquire(LockOwner leaser) throws ProcessLockException {
    if ((lockOwner != null) && (!lockOwner.equals(leaser))) {
      throw new ProcessLockException("Process is currently locked - probably in shutdown; try again");
    }
    lockOwner = leaser;
  }

  /**
   * Waits until the lock on the process is released, or until the given timeout
   * is reached.
   * 
   * @param timeout
   *          a timeout
   * @param timeUnit
   *          the {@link TimeUnit} in which the timeout is expressed.
   */
  public synchronized void awaitRelease(long timeout, TimeUnit timeUnit) throws InterruptedException {
    Delay delay = new Delay(timeout, timeUnit).start();
    while ((lockOwner != null) && delay.isNotOver()) {
      wait(delay.remainingMillis());
    }
  }

  /**
   * @return this instance's current {@link LockOwner}, or null if none is
   *         currently set.
   */
  public LockOwner getOwner() {
    return lockOwner;
  }

  /**
   * Forces the releases of the lock on this instance.
   */
  public synchronized void release() {
    lockOwner = null;
  }

  /**
   * @return <code>true</code> if this instance is locked.
   */
  public synchronized boolean isLocked() {
    return lockOwner != null;
  }

  /**
   * Releases this instance's lock, ONLY if the passed in instance is the owner
   * of the lock (otherwise, this method has no effect).
   * 
   * @param leaser
   *          the object that attempts to release this instance's locked.
   * 
   * @return <code>true</code> if the lock was released, <code>false</code>
   *         otherwise.
   */
  public synchronized boolean release(LockOwner leaser) {
    if ((lockOwner != null) && (lockOwner.equals(leaser))) {
      lockOwner = null;
      notifyAll();
      return true;
    }
    return false;
  }

}
