package org.sapia.corus.processor.task;

import java.io.IOException;

import org.sapia.corus.client.services.os.OsModule.KillSignal;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.processor.ProcessRepository;
import org.sapia.corus.processor.hook.ProcessContext;
import org.sapia.corus.processor.hook.ProcessHookManager;
import org.sapia.corus.taskmanager.TaskLogCallback;
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
      ProcessRepository processes = ctx.getServerContext().getServices().getProcesses();

      try {
        if (performOsKill && proc.getOsPid() != null) {
          ProcessHookManager processHooks = ctx.getServerContext().lookup(ProcessHookManager.class);
          processHooks.kill(new ProcessContext(proc), KillSignal.SIGKILL, new TaskLogCallback(ctx));
        }
      } catch (IOException e) {
        ctx.warn("Error caught trying to kill process", e);
      }

      synchronized (processes) {
        proc.setStatus(Process.LifeCycleStatus.SUSPENDED);
        proc.save();
      }

      ctx.warn(String.format("Process %s put in suspended process queue.", proc));
    } finally {
      super.abort(ctx);
    }
  }
}
