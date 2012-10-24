package org.sapia.corus.processor.task;

import java.io.IOException;

import org.sapia.corus.client.services.os.OsModule;
import org.sapia.corus.client.services.port.PortManager;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.processor.ProcessRepository;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;

/**
 * This task suspends an active process.
 * 
 * @author Yanick Duchesne
 */
public class SuspendTask extends KillTask {
  
  public SuspendTask(int maxRetry) {
    super(maxRetry);
  }
  
  @Override
  protected void doKillConfirmed(boolean performOsKill, TaskExecutionContext ctx) {
    try {
      PortManager ports = ctx.getServerContext().getServices().lookup(PortManager.class);
      ProcessRepository processes = ctx.getServerContext().getServices().getProcesses();
      
      try {
        OsModule os = ctx.getServerContext().lookup(OsModule.class);
        if (performOsKill && proc.getOsPid() != null) {
          os.killProcess(osKillCallback(), proc.getOsPid());
        }
      } catch (IOException e) {
        ctx.warn("Error caught trying to kill process", e);        
      }

      synchronized (processes) {
        proc.releasePorts(ports);
        proc.setStatus(Process.LifeCycleStatus.SUSPENDED);     
        processes.getSuspendedProcesses().addProcess(proc);
        processes.getActiveProcesses().removeProcess(proc.getProcessID());
      }

      ctx.warn(String.format("Process %s put in suspended process queue.", proc));
    } finally {
      super.abort(ctx);
    }
  }
}
