package org.sapia.corus.processor.task;

import org.sapia.corus.admin.services.port.PortManager;
import org.sapia.corus.admin.services.processor.Process;
import org.sapia.corus.admin.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.exceptions.LockException;
import org.sapia.corus.exceptions.LogicException;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;

/**
 * Absract class that provides convenient basic behavior for process-terminating tasks.
 * 
 * @author Yanick Duchesne
 *
 */
public abstract class ProcessTerminationTask extends Task {

  private String                      _corusPid;
  private ProcessTerminationRequestor _requestor;

  /**
   * @param maxRetry the maximum number of times this instance will try
   * to terminate a process.
   * @param retryInterval the interval between retries (in seconds).
   */
  public ProcessTerminationTask(
       ProcessTerminationRequestor requestor,
       String corusPid, 
       int maxRetry) {
    setMaxExecution(maxRetry);
    _requestor = requestor;
    _corusPid  = corusPid;
  }
  
  @Override
  public Object execute(TaskExecutionContext ctx) throws Throwable {
    try{
      ctx.getServerContext().getServices().getProcesses().getActiveProcesses().getProcess(_corusPid).acquireLock(this);
    } catch (LockException e) {
      ctx.error("Could not acquire lock on process: " + _corusPid);
      ctx.error(e);
      abort(ctx);
      return null;
    } catch (LogicException e) {
      ctx.error("VM identifier not found for: " + _corusPid);
      ctx.error(e);
      abort(ctx);
      return null;
    }
    try {
      Process proc = ctx.getServerContext()
        .getServices()
        .getProcesses()
        .getActiveProcesses()
        .getProcess(_corusPid);
      
      if (proc.getStatus() == Process.LifeCycleStatus.KILL_CONFIRMED) {
        proc.releasePorts(ctx.getServerContext().getServices().lookup(PortManager.class));
        onKillConfirmed(ctx);
        abort(ctx);
      } else {
        onExec(ctx);
      }
    } catch (LogicException e) {
      ctx.error(e);
      abort(ctx);
    }
    return null;
  }
  
  /**
   * @return the Corus process identifier of the process to terminate.
   */
  protected String corusPid() {
    return _corusPid;
  }

  /**
   * @return the logical identifier of the originator of the termination
   * request.
   */
  protected ProcessTerminationRequestor requestor() {
    return _requestor;
  }

  
  @Override
  protected void abort(TaskExecutionContext ctx) {
    try{
      ctx.getServerContext().getServices().getProcesses().getActiveProcesses().getProcess(_corusPid).releaseLock(this);
    }catch(Throwable err){
      //ctx.error(err);
    }finally{
      super.abort(ctx);
    }
  }
  
  /**
   * Template method that is called when the process corresponding
   * to this task has shut down.
   * @param ctx a {@link TaskExecutionContext}.
   */  
  protected abstract void onKillConfirmed(TaskExecutionContext ctx) throws Throwable;
  
  protected abstract void onExec(TaskExecutionContext ctx) throws Throwable;

}
