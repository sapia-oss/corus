package org.sapia.corus.processor.task;

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
 * This task perform a native OS kill on a process (on Linux/Unix, using
 * <code>kill -9</code>). As a matter of precaution, it releases all ports
 * currently held by the given process, if any.
 * 
 * @author yduchesne
 */
public class ForcefulKillTask extends Task<Boolean, TaskParams<Process, ProcessTerminationRequestor, Void, Void>> {

  @Override
  public Boolean execute(TaskExecutionContext ctx, TaskParams<Process, ProcessTerminationRequestor, Void, Void> params) throws Throwable {

    Process                     process   = params.getParam1();
    ProcessTerminationRequestor requestor = params.getParam2();
    
    ProcessHookManager   processHooks = ctx.getServerContext().lookup(ProcessHookManager.class);
    
    boolean killSuccess = true;

    ctx.warn(String.format("Process %s did not confirm kill; requestor: %s. Trying to perform a hard kill", process, requestor));

    // try forceful kill if OS pid not null...
    if (process.getOsPid() != null) {
      try {
        processHooks.kill(new ProcessContext(process), KillSignal.SIGKILL, new TaskLogCallback(ctx));
        process.setStatus(LifeCycleStatus.KILL_CONFIRMED);
        process.save();
      } catch (Throwable e) {
        ctx.warn(String.format("Error performing OS kill on process %s. Assumed to be terminated", process));
        ctx.error(e);
        process.setStatus(LifeCycleStatus.KILL_ASSUMED);
        process.save();
      }
    } else {
      ctx.warn(String.format("Process has no OS PID: %s; could bot be forcefully killed", process));
      killSuccess = false;
    }

    return killSuccess;
  }
}
