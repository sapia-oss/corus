package org.sapia.corus.processor.task;

import java.io.IOException;

import org.sapia.corus.client.services.os.OsModule;
import org.sapia.corus.client.services.port.PortManager;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
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

    Process process = params.getParam1();
    ProcessTerminationRequestor requestor = params.getParam2();
    PortManager ports = ctx.getServerContext().lookup(PortManager.class);
    OsModule os = ctx.getServerContext().lookup(OsModule.class);
    boolean killSuccess = true;

    ctx.warn(String.format("Process %s did not confirm kill; requestor: %s. Trying to perform a hard kill", process, requestor));

    // try forceful kill if OS pid not null...
    if (process.getOsPid() != null) {
      try {
        os.killProcess(callback(ctx), process.getOsPid());
        process.setStatus(LifeCycleStatus.KILL_CONFIRMED);
        process.save();
      } catch (IOException e) {
        ctx.warn(String.format("Error performing OS kill on process %s", process));
        ctx.error(e);
        killSuccess = false;
      }
    } else {
      ctx.warn(String.format("Process has no OS PID: %s; could bot be forcefully killed", process));
      killSuccess = false;
    }

    // releasing process ports right away as a precaution
    process.releasePorts(ports);
    return killSuccess;
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
