package org.sapia.corus.processor.task;

import org.sapia.corus.admin.services.port.PortManager;
import org.sapia.corus.admin.services.processor.Process;
import org.sapia.corus.admin.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.exceptions.LogicException;
import org.sapia.corus.processor.ProcessRepository;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;

/**
 * This task suspends an active process.
 * 
 * @author Yanick Duchesne
 */
public class SuspendTask extends ProcessTerminationTask {
  
  public SuspendTask(ProcessTerminationRequestor requestor, String corusPid, int maxRetry) {
    super(requestor, corusPid, maxRetry);
  }
  
  @Override
  protected void onExec(TaskExecutionContext ctx) {
    try {
      ProcessRepository processes = ctx.getServerContext().getServices().getProcesses();
      Process process = processes.getActiveProcesses().getProcess(corusPid());
      ProcessorTaskStrategy strategy = ctx.getServerContext().lookup(ProcessorTaskStrategy.class);
      
      if(strategy.attemptKill(ctx, requestor(), process, super.getExecutionCount())){
        abort(ctx);
      }
    } catch (Throwable e) {
      // no Vm for ID...
      super.abort(ctx);
      ctx.error(e);
    }
  }  

  @Override
  protected void onKillConfirmed(TaskExecutionContext ctx) {
    try {
      PortManager ports = ctx.getServerContext().getServices().lookup(PortManager.class);
      ProcessRepository processes = ctx.getServerContext().getServices().getProcesses();
      Process process = processes.getActiveProcesses().getProcess(corusPid());
      
      process.releasePorts(ports);

      synchronized (processes) {
        process.setStatus(Process.LifeCycleStatus.SUSPENDED);        
        processes.getSuspendedProcesses().addProcess(process);
        processes.getActiveProcesses().removeProcess(process.getProcessID());
      }

      ctx.warn("Process '" + process.getProcessID() + "' put in suspended process queue.");
    } catch (LogicException e) {
      ctx.error(e);
    } finally {
      super.abort(ctx);
    }
  }
  
  @Override
  protected void onMaxExecutionReached(TaskExecutionContext ctx)
      throws Throwable {
    ProcessorTaskStrategy strategy = ctx.getServerContext().lookup(ProcessorTaskStrategy.class);
    
    if (strategy.forcefulKill(ctx, requestor(), corusPid())) {
      onKillConfirmed(ctx);
    } 
  }
}
