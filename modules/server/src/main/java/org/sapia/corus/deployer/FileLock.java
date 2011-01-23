package org.sapia.corus.deployer;

import org.sapia.corus.client.exceptions.deployer.ConcurrentDeploymentException;


/**
 * A class that models a lock on a file. Used so that two concurrent threads
 * (representing two users) will not attempt to deploy the same archive.
 *
 * @author Yanick Duchesne
 */
public class FileLock {
  long    _start     = System.currentTimeMillis();
  long    _timeout;
  int     _waitCount;
  boolean _locked;
  String  _fName;

  public FileLock(String fName, long timeout) {
    _fName   = fName;
    _timeout = timeout;
  }

  public synchronized boolean isOver() {
    return ((System.currentTimeMillis() - _start) > (_timeout * 2)) &&
           (_waitCount == 0);
  }

  public synchronized void forceUnlock() {
    _locked = false;
    notifyAll();
  }

  public synchronized void acquire()
                            throws ConcurrentDeploymentException, 
                                   InterruptedException {
    long start = System.currentTimeMillis();

    if (_locked) {
      _waitCount++;

      while (_locked) {
        wait(_timeout);

        if ((System.currentTimeMillis() - start) > _timeout) {
          _waitCount--;

          if (_waitCount < 0) {
            _waitCount = 0;
          }

          throw new ConcurrentDeploymentException(_fName);
        }
      }
    }

    _waitCount--;

    if (_waitCount < 0) {
      _waitCount = 0;
    }

    _locked = true;
    _start  = System.currentTimeMillis();
    notifyAll();
  }
  
  public int getWaitingCount() {
    return _waitCount;
  }

  public synchronized void release() {
    _locked = false;
    notify();
  }
}
