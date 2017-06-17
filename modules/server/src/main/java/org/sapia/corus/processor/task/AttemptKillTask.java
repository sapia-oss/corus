package org.sapia.corus.processor.task;

import java.io.IOException;

import org.sapia.corus.client.common.ToStringUtil;
import org.sapia.corus.client.services.os.OsModule.KillSignal;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.processor.hook.ProcessContext;
import org.sapia.corus.processor.hook.ProcessHookManager;
import org.sapia.corus.taskmanager.TaskLogCallback;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskParams;

/**
 * Attempts soft-killing a given process: triggers the sending of the kill
 * command to the process.
 * 
 * @author yduchesne
 */
public class AttemptKillTask extends Task<Boolean, TaskParams<Process, ProcessTerminationRequestor, Integer, Integer>> {

  /**
   * Returns <code>true</code> if the process has confirmed its shutdown,
   * <code>false</code> otherwise.
   */
  @Override
  public Boolean execute(TaskExecutionContext ctx, TaskParams<Process, ProcessTerminationRequestor, Integer, Integer> params) throws Throwable {

    ProcessHookManager processHooks = ctx.getServerContext().lookup(ProcessHookManager.class);
    Process proc = params.getParam1();
    ProcessTerminationRequestor requestor = params.getParam2();
    int currentRetryCount = params.getParam3().intValue();
    int currentMaxExec    = params.getParam4().intValue();

    if (proc.getStatus() == Process.LifeCycleStatus.KILL_CONFIRMED) {
      ctx.info(String.format("Process %s has confirmed shutdown", proc));
      return true;
    } else if (proc.getStatus() == Process.LifeCycleStatus.KILL_ASSUMED) {
      ctx.info(String.format("Process %s is assumed terminated", proc));
      return true;
    } 
     
    if (proc.isInteropEnabled()) {
      ctx.info(String.format("Attempting to kill process %s. Attempt: %s; requestor %s", proc, currentRetryCount, requestor));
      proc.kill(requestor);
      // try SIGTERM if we're at the before last attempt
      if (currentRetryCount >= currentMaxExec - 1) {
        ctx.debug("Execution count is: " + currentRetryCount + "; max executions: " + currentMaxExec);
        try {
          processHooks.kill(new ProcessContext(proc), KillSignal.SIGTERM, new TaskLogCallback(ctx));
        } catch (Throwable e) {
          ctx.error("Error trying to kill process " + ToStringUtil.toString(proc) +  ". Assuming process already terminated", e);
          proc.setStatus(LifeCycleStatus.KILL_ASSUMED);
          return true;
        }
      }
    } else {
      ctx.info(String.format("Attempting to kill process %s. Attempt: %s; requestor %s", proc, currentRetryCount, requestor));
      try {
        processHooks.kill(new ProcessContext(proc), KillSignal.SIGTERM, new TaskLogCallback(ctx));
      } catch (Throwable e) {
        ctx.error("Error trying to kill process " + ToStringUtil.toString(proc) +  ". Assuming process already terminated", e);
        proc.setStatus(LifeCycleStatus.KILL_ASSUMED);
        return true;
      }
    }
    proc.save();
    return false;
  }
}
