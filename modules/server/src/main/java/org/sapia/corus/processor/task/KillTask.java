package org.sapia.corus.processor.task;

import java.io.IOException;

import org.sapia.corus.client.exceptions.processor.ProcessLockException;
import org.sapia.corus.client.services.os.OsModule;
import org.sapia.corus.client.services.port.PortManager;
import org.sapia.corus.client.services.processor.LockOwner;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.client.services.processor.ProcessorConfiguration;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.client.services.processor.event.ProcessKilledEvent;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskParams;

/**
 * This task insures the destruction of a given process: it attempts performing
 * a 'soft' kill (trying to send a kill command through the Corus IOP protocol).
 * Such attempts will occur up to a predefined amount of times. If after these
 * retries the process has not confirmed its shutdown, this task resorts to an
 * OS kill.
 * <p>
 * In addition, if the process termination had originally been ordered by the
 * Corus server itself, this task will automatically attempt triggering the
 * process restart.
 * <p>
 * Note that this tasks acquires the process lock while it is executed.
 * <p>
 * Calls:
 * <ol>
 * <li> {@link AttemptKillTask}: in order to perform the process soft kill
 * attempts.
 * <li> {@link PerformProcessRestartTask}: in order to restart the killed
 * process.
 * </ol>
 * 
 * @author Yanick Duchesne
 */
public class KillTask extends Task<Void, TaskParams<Process, ProcessTerminationRequestor, Void, Void>> {

  /**
   * The {@link Process} to kill.
   */
  protected Process proc;

  /**
   * The {@link ProcessTerminationRequestor} identifying from whom the kill
   * order originates.
   */
  protected ProcessTerminationRequestor requestor;

  /**
   * The {@link LockOwner} used to acquired a lock on the process.
   */
  protected LockOwner lockOwner = LockOwner.createInstance();

  /**
   * The flag indicating if a hard kill should be performed.
   */
  protected boolean hardKill;
  
  /**
   * Constructs an instance of this class with the given params.
   */
  public KillTask(int maxRetry) {
    super.setMaxExecution(maxRetry);
  }
  
  /**
   * @param hardKill if <code>true</code>, indicates that the process should be terminated using
   * an OS kill.
   * @return this instance.
   */
  public KillTask setHardKill(boolean hardKill) {
    this.hardKill = hardKill;
    return this;
  }

  @Override
  public Void execute(TaskExecutionContext ctx, TaskParams<Process, ProcessTerminationRequestor, Void, Void> params) throws Throwable {

    proc = params.getParam1();
    requestor = params.getParam2();

    ctx.debug(String.format("Killing %s", proc));

    // refreshing - another thread might have updated the process instance
    // since last execution (for example, process might have confirmed kill)
    proc.refresh();

    // acquiring lock on process (might already be acquired from
    // previous execution - in this case no side effects)
    try {
      proc.getLock().acquire(lockOwner);
      proc.save();
    } catch (ProcessLockException e) {
      ctx.error(String.format("Could not acquire lock on process: %s", proc));
      ctx.error(e);
      abort(ctx);
      return null;
    }

    // lock acquired, checking if process had confirmed previous kill
    // (if so, no point in continuing)
    if (proc.getStatus() == Process.LifeCycleStatus.KILL_CONFIRMED) {
      doKillConfirmed(true, ctx);
    } else {
      if (hardKill) {
        ctx.debug(String.format("Proceeding to hard kill on process %s", proc));
        ctx.getTaskManager().executeAndWait(new ForcefulKillTask(), TaskParams.createFor(proc, requestor)).get();
        doKillConfirmed(false, ctx);
      } else {
        ctx.getTaskManager().executeAndWait(new AttemptKillTask(), TaskParams.createFor(proc, requestor, super.getExecutionCount())).get();
      }
    }

    return null;
  }

  @Override
  protected void abort(TaskExecutionContext ctx) {
    try {
      ctx.debug(String.format("Releasing lock on: %s", proc));
      proc.getLock().release(lockOwner);
      if (proc.getStatus() != LifeCycleStatus.KILL_CONFIRMED) {
        proc.save();
      }
    } catch (Throwable err) {
      // noop
    } finally {
      super.abort(ctx);
    }
  }

  // maximum kill retry has been reached, proceed to hard kill
  @Override
  protected void onMaxExecutionReached(TaskExecutionContext ctx) throws Throwable {

    if (ctx.getTaskManager().executeAndWait(new ForcefulKillTask(), TaskParams.createFor(proc, requestor)).get()) {
      doKillConfirmed(false, ctx);
    } else {
      PortManager ports = ctx.getServerContext().getServices().lookup(PortManager.class);
      ctx.error(String.format("Process %s could not be killed forcefully; auto-restart is aborted", proc));
      proc.releasePorts(ports);
      proc.save();
      ctx.getServerContext().getServices().getProcesses().getActiveProcesses().removeProcess(proc.getProcessID());
      if (requestor == ProcessTerminationRequestor.KILL_REQUESTOR_SERVER) {
        ctx.getServerContext().getServices().getEventDispatcher().dispatch(new ProcessKilledEvent(requestor, proc, false));
      }
      abort(ctx);
    }
  }

  protected void doKillConfirmed(boolean performOsKill, TaskExecutionContext ctx) throws Throwable {
    try {
      ctx.info(String.format("Process kill (by %s) confirmed for %s", requestor, proc));

      ProcessorConfiguration procConfig = ctx.getServerContext().getServices().lookup(Processor.class).getConfiguration();
      PortManager ports = ctx.getServerContext().getServices().getPortManager();
      proc.releasePorts(ports);
      
      try {
        OsModule os = ctx.getServerContext().lookup(OsModule.class);
        if (performOsKill && proc.getOsPid() != null) {
          os.killProcess(osKillCallback(), proc.getOsPid());
        }
      } catch (IOException e) {
        ctx.warn("Error caught trying to hard kill process as part of cleanup (process probably absent, so it properly shut down)", e);
      }

      ctx.getTaskManager().executeAndWait(new CleanupProcessTask(), proc).get();
      if (requestor == ProcessTerminationRequestor.KILL_REQUESTOR_SERVER) {
        if ((System.currentTimeMillis() - proc.getCreationTime()) > procConfig.getRestartIntervalMillis()) {
          ctx.warn(String.format("Restarting process: %s", proc));
          PerformProcessRestartTask restartProcess = new PerformProcessRestartTask();
          ctx.getTaskManager().executeAndWait(restartProcess, proc).get();
          ctx.getServerContext().getServices().getEventDispatcher().dispatch(new ProcessKilledEvent(requestor, proc, true));
        } else {
          ctx.debug(String.format("Restarting interval (millis): %s", procConfig.getRestartIntervalMillis()));
          ctx.warn("Process will not be restarted; not enough time since last restart");
          ctx.getServerContext().getServices().getEventDispatcher().dispatch(new ProcessKilledEvent(requestor, proc, false));
        }
      } else {
        ctx.warn(String.format("Process %s terminated", proc));
      }
    } finally {
      abort(ctx);
    }
  }

  protected OsModule.LogCallback osKillCallback() {
    return new OsModule.LogCallback() {
      @Override
      public void error(String msg) {
      }

      @Override
      public void debug(String msg) {
      }
    };
  }
}
