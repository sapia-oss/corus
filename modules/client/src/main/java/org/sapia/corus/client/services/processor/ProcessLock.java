package org.sapia.corus.client.services.processor;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.concurrent.TimeUnit;

import org.sapia.corus.client.common.Delay;
import org.sapia.corus.client.exceptions.processor.ProcessLockException;
import org.sapia.ubik.util.Strings;

/**
 * Models a lock on a {@link Process}.
 * 
 * @author yduchesne
 * 
 */
public class ProcessLock implements Externalizable {

  static final long serialVersionUID = 1L;

  private LockOwner lockOwner;

  /**
   * Acquires the lock on this instance.
   * 
   * @param leaser
   *          the object that attempts to obtain the lock on this instance.
   * @throws ProcessLockException
   *           if this instance is already locked by another object.
   */
  public synchronized void acquire(LockOwner leaser) throws ProcessLockException {
    if (lockOwner != null) {
      if (lockOwner.isExclusive()) {
        if (!lockOwner.equals(leaser)) {
          throw new ProcessLockException("Process is currently locked - probably in shutdown; try again");
        }
      } else {
        // we leave the ownership to the non-exclusive owner.
        return;
      }
    } else {
      lockOwner = leaser;
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
    while (lockOwner != null && delay.isNotOver()) {
      wait(delay.remainingMillisNotZero());
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
   * @return <code>true</code> if this instance is locked by an exclusive {@link LockOwner}.
   */
  public synchronized boolean isLocked() {
    return lockOwner != null && lockOwner.isExclusive();
  }
  
  /**
   * @return <code>true</code> if this instance is locked by a non-exclusive {@link LockOwner}.
   */
  public synchronized boolean isShared() {
    return lockOwner != null && !lockOwner.isExclusive();
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
    if (lockOwner != null) {
      if (lockOwner.isExclusive()) {
        if (lockOwner.equals(leaser)) {
          release();
          notifyAll();
          return true;
        } else {
          return false;
        }
      } else {
        release();
        notifyAll();
        return true;
      }
    }
    return true;
  }
  
  @Override
  public void readExternal(ObjectInput in) throws IOException,
      ClassNotFoundException {
    lockOwner = (LockOwner) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(lockOwner);
  }
  
  @Override
  public String toString() {
    return Strings.toString("owner", lockOwner);
  }

}
