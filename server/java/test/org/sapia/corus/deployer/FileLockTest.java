package org.sapia.corus.deployer;

import junit.framework.TestCase;


/**
 * @author Yanick Duchesne
 * 2002-02-23
 */
public class FileLockTest extends TestCase {
  public FileLockTest(String name) {
    super(name);
  }

  public void testCannotAcquire() throws Exception {
    FileLock lock = new FileLock("test", 500);
    lock.acquire();

    Concurrent c = new Concurrent(lock, 500);
    Thread.sleep(1500);
    lock.release();

    if (c.getErr() == null) {
      throw new Exception("concurrent exception not signaled");
    }
  }

  public void testCanAcquire() throws Exception {
    FileLock lock = new FileLock("test", 3000);
    lock.acquire();

    Concurrent c = new Concurrent(lock, 1000);
    Thread.sleep(1000);
    lock.release();

    if (c.getErr() != null) {
      throw new Exception("concurrent exception signaled");
    }
  }

  public void testWaitCount() throws Exception {
    FileLock lock = new FileLock("test", 3000);
    lock.acquire();

    Concurrent c1 = new Concurrent(lock, 1000);
    Concurrent c2 = new Concurrent(lock, 1000);
    
    Thread.sleep(500);    
    if (lock.getWaitingCount() != 2) {
      throw new Exception("2 threads should be acquiring lock");
    }
    
    Thread.sleep(1500);

    lock.release();
    Thread.sleep(3000);

    if (lock.getWaitingCount() != 0) {
      throw new Exception("no thread should be acquiring lock");
    }
  }

  public void testIsOver() throws Exception {
    FileLock lock = new FileLock("test", 300);
    lock.acquire();

    Concurrent c1 = new Concurrent(lock, 1000);
    Thread.sleep(1000);
    super.assertTrue(lock.isOver());
    lock.forceUnlock();
  }

  class Concurrent implements Runnable {
    FileLock  _lock;
    long      _keep;
    Throwable _err;

    Concurrent(FileLock lock, long keep) {
      _lock = lock;
      _keep = keep;

      Thread t = new Thread(this);
      t.start();
    }

    public void run() {
      try {
        System.out.println("Acquiring file lock " + this);        
        _lock.acquire();
        System.out.println("Acquired file lock" + this);                
        Thread.sleep(_keep);
        System.out.println("Releasing file lock" + this);
        _lock.release();
      }catch (Throwable t) {
        _err = t;
      } 
    }

    Throwable getErr() {
      return _err;
    }
  }
}
