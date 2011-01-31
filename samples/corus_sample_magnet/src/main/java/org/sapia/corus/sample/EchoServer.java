package org.sapia.corus.sample;

import java.sql.Timestamp;

import org.sapia.corus.interop.Context;
import org.sapia.corus.interop.Param;
import org.sapia.corus.interop.Status;
import org.sapia.corus.interop.api.InteropLink;
import org.sapia.corus.interop.api.StatusRequestListener;

public class EchoServer implements StatusRequestListener {

  private long _initialWaitDelayMillis;
  
  private long _loopIntervalMillis;
  
  private String _name;
  
  private EchoTask _task;
  private long _loopCount;
  
  /**
   * Creates a new {@link EchoServer} instance.
   */
  public EchoServer() {
    // Add listener to publish the server status to corus
    InteropLink.getImpl().addStatusRequestListener(this);
  }

  /**
   * Returns the initialWaitDelayMillis attribute.
   *
   * @return The initialWaitDelayMillis value.
   */
  public long getInitialWaitDelayMillis() {
    return _initialWaitDelayMillis;
  }

  /**
   * Changes the value of the attributes initialWaitDelayMillis.
   *
   * @param aInitialWaitDelayMillis The new value of the initialWaitDelayMillis attribute.
   */
  public void setInitialWaitDelayMillis(long aInitialWaitDelayMillis) {
    _initialWaitDelayMillis = aInitialWaitDelayMillis;
  }

  /**
   * Returns the loopIntervalMillis attribute.
   *
   * @return The loopIntervalMillis value.
   */
  public long getLoopIntervalMillis() {
    return _loopIntervalMillis;
  }

  /**
   * Changes the value of the attributes loopIntervalMillis.
   *
   * @param aLoopIntervalMillis The new value of the loopIntervalMillis attribute.
   */
  public void setLoopIntervalMillis(long aLoopIntervalMillis) {
    _loopIntervalMillis = aLoopIntervalMillis;
  }

  /**
   * Returns the name attribute.
   *
   * @return The name value.
   */
  public String getName() {
    return _name;
  }

  /**
   * Changes the value of the attributes name.
   *
   * @param aName The new value of the name attribute.
   */
  public void setName(String aName) {
    _name = aName;
  }

  /**
   * Starts this echo task
   */
  public synchronized void start() {
    if (_task == null) {
      _task = new EchoTask();
      Thread t = new Thread(_task);
      t.setDaemon(true);
      t.start();
    }
  }
  
  /**
   * Stops this echo task
   */
  public synchronized void stop() {
    if (_task != null) {
      try {
        System.out.println("Stopping echo server " + _name + "...");
        _task.stop();
      } catch (Exception e) {
      } finally {
        _task = null;
        System.out.println("Echo server " + _name + " is stopped");
      }
    }
  }
  
  /* (non-Javadoc)
   * @see org.sapia.corus.interop.api.StatusRequestListener#onStatus(org.sapia.corus.interop.Status)
   */
  @Override
  public void onStatus(Status aStatus) {
    Context context = new Context("org.sapia.corus.sample.EchoServer#"+_name);
    context.addParam(new Param("initialWaitDelayMillis", String.valueOf(_initialWaitDelayMillis)));
    context.addParam(new Param("loopIntervalMillis", String.valueOf(_loopIntervalMillis)));
    context.addParam(new Param("isRunning", String.valueOf(_task != null)));
    context.addParam(new Param("loopCount", String.valueOf(_loopCount)));
    
    aStatus.addContext(context);
  }


  /**
   * Inner class that performs the asynchronous task of "echoing" it's name on the standard output.
   * 
   * @author J-C Desrochers
   */
  public class EchoTask implements Runnable {
    
    private Thread _daemon;
    private boolean _isStopped;
    
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
      _daemon = Thread.currentThread();
      _isStopped = false;
      _loopCount = 0;
      
      try {
        System.out.println("Starting echo server " + _name + " : waiting " + _initialWaitDelayMillis + " millis...");
        Thread.sleep(_initialWaitDelayMillis);
        
        while (!_isStopped) {
          StringBuilder buffer = new StringBuilder().
                  append(new Timestamp(System.currentTimeMillis())).append("  -->  ").append(_name);
          System.out.println(buffer.toString());
          
          _loopCount++;
          Thread.sleep(_loopIntervalMillis);
        }
      } catch (Exception e) {

      }
    }
    
    public synchronized void stop() throws InterruptedException {
      if (_daemon != null && !_isStopped) {
        _isStopped = true;
        _daemon.interrupt();
        _daemon.join();
      }
    }
    
  }
  
}
