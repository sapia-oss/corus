package org.sapia.corus.processor.task;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.sapia.corus.client.exceptions.processor.ProcessLockException;
import org.sapia.corus.client.services.os.OsModule.KillSignal;
import org.sapia.corus.client.services.processor.LockOwner;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.client.services.processor.ProcessorConfiguration;
import org.sapia.corus.client.services.processor.event.ProcessAssumedKilledEvent;
import org.sapia.corus.client.services.processor.event.ProcessKillPendingEvent;
import org.sapia.corus.client.services.processor.event.ProcessKilledEvent;
import org.sapia.corus.processor.hook.ProcessContext;
import org.sapia.corus.processor.hook.ProcessHookManager;
import org.sapia.corus.taskmanager.TaskLogCallback;
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
    // Ajusting the semantic meaning of max execution count vs. max retry attempt by adding one attempt
    super.setMaxExecution(1 + maxRetry);
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

    proc      = params.getParam1();
    requestor = params.getParam2();
    
    if (super.getExecutionCount() == 0) {
       ctx.getServerContext().getServices().getEventDispatcher().dispatch(new ProcessKillPendingEvent(requestor, proc));
       ctx.getTaskManager().executeAndWait(new UnpublishProcessTask(), proc).get();
    }

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
      ctx.warn(String.format("Could not acquire lock on process: %s", proc));
      ctx.warn("Details:", e);
      abort(ctx);
      return null;
    }

    // lock acquired, checking if process had confirmed previous kill
    // (if so, no point in continuing)
    if (proc.getStatus() == Process.LifeCycleStatus.KILL_CONFIRMED || proc.getStatus() == LifeCycleStatus.KILL_ASSUMED) {
      doKillConfirmed(true, ctx);
    } else {
      if (hardKill) {
        ctx.debug(String.format("Proceeding to hard kill on process %s", proc));
        ctx.getTaskManager().executeAndWait(new ForcefulKillTask(), TaskParams.createFor(proc, requestor)).get();
        doKillConfirmed(false, ctx);
      } else {
        ctx.getTaskManager().executeAndWait(new AttemptKillTask(), 
            TaskParams.createFor(proc, requestor, super.getExecutionCount(), super.getMaxExecution())).get();
      }
    }
    return null;
  }

  @Override
  protected void abort(TaskExecutionContext ctx) {
    try {
      ctx.debug(String.format("Releasing lock on: %s", proc));
      proc.getLock().release(lockOwner);
      if (proc.getStatus() != LifeCycleStatus.KILL_CONFIRMED && proc.getStatus() != LifeCycleStatus.KILL_ASSUMED) {
        proc.save();
      } else {
        proc.delete();
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
      ctx.error(String.format("Process %s could not be killed forcefully; auto-restart is aborted", proc));
      proc.save();
      ctx.getServerContext().getServices().getProcesses().removeProcess(proc.getProcessID());
      if (requestor == ProcessTerminationRequestor.KILL_REQUESTOR_SERVER) {
        ctx.getServerContext().getServices().getEventDispatcher().dispatch(new ProcessKilledEvent(requestor, proc, false));
      }
      abort(ctx);
    }
  }

  protected void doKillConfirmed(boolean performOsKill, TaskExecutionContext ctx) throws Throwable {
    try {
      ctx.info(String.format("Process kill (by %s) confirmed for %s", requestor, proc));

      ProcessHookManager   processHooks = ctx.getServerContext().lookup(ProcessHookManager.class);
      ProcessorConfiguration procConfig   = ctx.getServerContext().getServices().lookup(Processor.class).getConfiguration();
      
      try {
        if (performOsKill && proc.getOsPid() != null) {
          processHooks.kill(new ProcessContext(proc), KillSignal.SIGKILL, new TaskLogCallback(ctx));
        }
      } catch (Throwable e) {
        ctx.warn("Error caught trying to hard kill process as part of cleanup (process probably absent, so it properly shut down)", e);
      }

      ctx.getTaskManager().executeAndWait(new CleanupProcessTask(), proc).get();

      if (requestor == ProcessTerminationRequestor.KILL_REQUESTOR_SERVER) {
        if ((System.currentTimeMillis() - proc.getCreationTime()) > procConfig.getRestartIntervalMillis()) {
          ctx.warn(String.format("Restarting process: %s. Created at %s - min running time (secs): %s", 
              proc, new Date(proc.getCreationTime()), 
              TimeUnit.SECONDS.convert(procConfig.getRestartIntervalMillis(), TimeUnit.MILLISECONDS))
          );
          if (proc.getStatus() == LifeCycleStatus.KILL_CONFIRMED) {
            ctx.getServerContext().getServices().getEventDispatcher().dispatch(new ProcessKilledEvent(requestor, proc, true));
          } else if (proc.getStatus() == LifeCycleStatus.KILL_ASSUMED) {
            ctx.getServerContext().getServices().getEventDispatcher().dispatch(new ProcessAssumedKilledEvent(requestor, proc, true));            
          }
          PerformProcessRestartTask restartProcess = new PerformProcessRestartTask();
          ctx.getTaskManager().executeAndWait(restartProcess, proc).get();
        } else {
          ctx.debug(String.format("Restart interval (secs): %s", 
              TimeUnit.SECONDS.convert(procConfig.getRestartIntervalMillis(), TimeUnit.MILLISECONDS)));
          ctx.warn(String.format("Process will not be restarted; not enough time since last restart at: %s", 
              new Date(proc.getCreationTime())));
          if (proc.getStatus() == LifeCycleStatus.KILL_CONFIRMED) {
            ctx.getServerContext().getServices().getEventDispatcher().dispatch(new ProcessKilledEvent(requestor, proc, false));
          } else if (proc.getStatus() == LifeCycleStatus.KILL_ASSUMED) {
            ctx.getServerContext().getServices().getEventDispatcher().dispatch(new ProcessAssumedKilledEvent(requestor, proc, false));            
          }
        }
      } else {
        if (proc.getStatus() == LifeCycleStatus.KILL_CONFIRMED) {
          ctx.getServerContext().getServices().getEventDispatcher().dispatch(new ProcessKilledEvent(requestor, proc, false));
        } else if (proc.getStatus() == LifeCycleStatus.KILL_ASSUMED) {
          ctx.getServerContext().getServices().getEventDispatcher().dispatch(new ProcessAssumedKilledEvent(requestor, proc, false));            
        }
        ctx.warn(String.format("Process %s terminated", proc));
      }
    } finally {
      abort(ctx);
    }
  }
}
