package org.sapia.corus.processor.task;

import java.util.List;

import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.client.services.processor.ProcessorConfiguration;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;


/**
 * This task ensures that all external processes are up and running. It determines
 * so by checking the time at which each process last polled its Corus
 * server. This task is ran continuously at a predefined interval.
 *
 * @author Yanick Duchesne
 */
public class ProcessCheckTask extends Task{
  
  
  @Override
  public Object execute(TaskExecutionContext ctx) throws Throwable {
    ctx.debug("Checking for stale processes...");

    ProcessorConfiguration processorConf = ctx.getServerContext().lookup(Processor.class).getConfiguration();
    ProcessorTaskStrategy  strategy      = ctx.getServerContext().lookup(ProcessorTaskStrategy.class);
    List<Process>          processes     = ctx.getServerContext().getServices().getProcesses().getActiveProcesses().getProcesses();
    Process proc;

    for (int i = 0; i < processes.size(); i++) {
      proc = processes.get(i);
      if ((proc.getStatus() == Process.LifeCycleStatus.ACTIVE) &&
            proc.isTimedOut(processorConf.getProcessTimeoutMillis())) {
        if (proc.isLocked()) {
          ctx.warn("Process timed out but locked, probably terminating or restarting: " + proc);
        } else {
          proc.setStatus(Process.LifeCycleStatus.KILL_REQUESTED);
          proc.save();

          ctx.warn("Process timed out - ordering kill: " + proc);
          strategy.killProcess(ctx, Process.ProcessTerminationRequestor.KILL_REQUESTOR_SERVER, proc);
          onTimeout(ctx);
        }
      } else if (proc.getStatus() == Process.LifeCycleStatus.KILL_CONFIRMED) {
        // will cleanup process dir, remove process from 
        // process store.
        if (!proc.isLocked()) {
          ctx.warn("Process not locked, cleaning up: " + proc);
          strategy.killConfirmed(ctx, proc);
        }
      }
    }

    ctx.debug("Stale process check finished");
    
    return null;
  }
  
  protected void onTimeout(TaskExecutionContext ctx){}
}
