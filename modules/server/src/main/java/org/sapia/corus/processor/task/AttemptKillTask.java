package org.sapia.corus.processor.task;

import org.sapia.corus.client.services.os.OsModule;
import org.sapia.corus.client.services.os.OsModule.KillSignal;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
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

    Process proc = params.getParam1();
    ProcessTerminationRequestor requestor = params.getParam2();
    int currentRetryCount = params.getParam3().intValue();
    int currentMaxExec    = params.getParam4().intValue();

    if (proc.getStatus() == Process.LifeCycleStatus.KILL_CONFIRMED) {
      ctx.info(String.format("Process %s has confirmed shutdown", proc));
      return true;
    }
    ctx.info(String.format("Attempting to kill process %s. Attempt: %s; requestor %s", proc, currentRetryCount, requestor));
    proc.kill(requestor);
    
    // try SIGTERM if we're at the before last attempt
    if (currentRetryCount >= currentMaxExec - 1) {
      ctx.debug("Execution count is: " + super.getExecutionCount() + "; max executions: " + super.getMaxExecution());
      OsModule os = ctx.getServerContext().lookup(OsModule.class);
      os.killProcess(callback(ctx), KillSignal.SIGTERM, proc.getOsPid());
    }
    proc.save();
    return false;
  }

  private OsModule.LogCallback callback(final TaskExecutionContext ctx) {
    return new OsModule.LogCallback() {

      @Override
      public void error(String msg) {
        ctx.error(msg);
      }

      @Override
      public void debug(String msg) {
        ctx.debug(msg);
      }
    };
  }
}
