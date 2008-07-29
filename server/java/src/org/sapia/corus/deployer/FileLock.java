package org.sapia.corus.deployer;


/**
 * A class that models a lock on a file. Used so that two concurrent threads
 * (representing two users) will not attempt to deploy the same archive.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
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
