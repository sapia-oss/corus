package org.sapia.corus.processor.task;

import org.sapia.corus.LogicException;
import org.sapia.corus.port.PortManager;
import org.sapia.corus.processor.LockException;
import org.sapia.corus.processor.Process;
import org.sapia.corus.processor.ProcessDB;
import org.sapia.taskman.RetryTask;
import org.sapia.taskman.TaskContext;
import org.sapia.ubik.net.TCPAddress;

/**
 * Absract class that provides convenient basic behavior for process-terminating tasks.
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public abstract class ProcessTerminationTask extends RetryTask {
  /**
   * This constant specifies the default maximum amount of times
   * the Corus server should try killing a given process; its value
   * is 3.
   */
  public static final int DEFAULT_MAX_RETRY = 3;

  /**
   * This constant speficies the default delay (in seconds)
   * between each VM kill attempt; value is 10 (seconds).
   */
  public static final int DEFAULT_RETRY_INTERVAL = 10;
  private String            _corusPid;
  private String            _requestor;
  private ProcessDB         _db;
  private TCPAddress        _dynSvr;
  private int               _httpPort;
  private PortManager       _ports;

  /**
   * @param maxRetry the maximum number of times this instance will try
   * to terminate a process.
   * @param retryInterval the interval between retries (in seconds).
   */
  public ProcessTerminationTask(TCPAddress dynSvrAddress, int httpPort, String requestor,
                                String corusPid, ProcessDB db, int maxRetry,
                                PortManager ports) {
    super(maxRetry+1);
    _dynSvr    = dynSvrAddress;
    _httpPort  = httpPort;
    _db        = db;
    _requestor = requestor;
    _corusPid  = corusPid;
    _ports     = ports;
  }

  public final void doExec(TaskContext ctx) {
    try {
      _db.getActiveProcesses().getProcess(_corusPid).acquireLock(this);
    } catch (LockException e) {
      ctx.getTaskOutput().error("Could not acquire lock on process: " + _corusPid);
      ctx.getTaskOutput().error(e);
      abort();
    } catch (LogicException e) {
      ctx.getTaskOutput().error("VM identifier not found for: " + _corusPid);
      ctx.getTaskOutput().error(e);
      abort();
    }

    if (getRetryCount() == getMaxRetries() - 1) {
      onMaxRetry(ctx);
    } else if (getRetryCount() >= getMaxRetries()) {
      abort();

      return;
    } else {
      try {
        Process proc = _db.getActiveProcesses().getProcess(_corusPid);
        if (proc.getStatus() == Process.KILL_CONFIRMED) {
          proc.releasePorts(_ports);
          onKillConfirmed(ctx);
          abort();
        } else {
          onExec(ctx);
        }
      } catch (LogicException e) {
        ctx.getTaskOutput().error(e);
        abort();
      }
    }
  }
  
  protected PortManager getPorts(){
    return _ports;
  }

  /**
   * @return the <code>ProcessDB</code> that holds the <code>Process</code>
   * instances in different states.
   */
  protected ProcessDB db() {
    return _db;
  }

  /**
   * @return the Corus process identifier of the process to terminate.
   */
  protected String corusPid() {
    return _corusPid;
  }

  /**
   * @return the address of the corus server in which this instance lives.
   */
  protected TCPAddress dynAddress() {
    return _dynSvr;
  }
  
  /**
   * @return the http port of the corus server in which this instance lives.
   */
  protected int httpPort(){
    return _httpPort;
  }

  /**
   * @return the logical identifier of the originator of the termination
   * request.
   */
  protected String requestor() {
    return _requestor;
  }

  /**
   * @see org.sapia.taskman.RetryTask#abort()
   */
  protected void abort() {
    try {
      _db.getActiveProcesses().getProcess(_corusPid).releaseLock(this);
    } catch (LogicException e) {
      // noop
    }

    super.abort();
  }
  
  /**
   * Template method that is called when normal execution 
   * should take place.
   * @param ctx a <code>TaskContext</code>.
   */
  protected abstract void onExec(TaskContext ctx);

  /**
   * Template method that is called when the maximum number
   * of retries has been reached and the process corresponding
   * to this task has not yet confirmed its shutdown.
   * @param ctx a <code>TaskContext</code>.
   */  
  protected abstract boolean onMaxRetry(TaskContext ctx);
  
  /**
   * Template method that is called when the process corresponding
   * to this task has shut down.
   * @param ctx a <code>TaskContext</code>.
   */  
  protected abstract void onKillConfirmed(TaskContext ctx);
}
