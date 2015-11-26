package org.sapia.corus.processor.task;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.services.diagnostic.DiagnosticModule;
import org.sapia.corus.client.services.diagnostic.ProcessDiagnosticResult;
import org.sapia.corus.client.services.processor.LockOwner;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.LifeCycleStatus;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.client.services.processor.ProcessorConfiguration;
import org.sapia.corus.client.services.processor.event.ProcessStaleEvent;
import org.sapia.corus.taskmanager.core.BackgroundTaskConfig;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskParams;

/**
 * This task ensures that all external processes are up and running. It
 * determines so by checking the time at which each process last polled its
 * Corus server. This task is ran continuously at a predefined interval.
 * <p>
 * At runtime, the task goes through currently active processes to determine the
 * ones that are 'down'. For these, it orders a kill/restart.
 * <p>
 * Calls:
 * <ol>
 * <li> {@link KillTask}: when down processes are detected.
 * </ol>
 * 
 * 
 * @author Yanick Duchesne
 */
public class ProcessCheckTask extends Task<Void, Void> {

  private LockOwner lockOwner = LockOwner.createInstance();
  
  @Override
  public Void execute(TaskExecutionContext ctx, Void param) throws Throwable {
    ctx.debug("Checking for stale processes...");

    Processor              processor     = ctx.getServerContext().getServices().getProcessor();
    DiagnosticModule       diagnostics   = ctx.getServerContext().getServices().getDiagnosticModule();
    ProcessorConfiguration processorConf = processor.getConfiguration();
    
    List<Process> processes = ctx.getServerContext().getServices().getProcesses().getProcesses(
        ProcessCriteria.builder().lifecycles(LifeCycleStatus.ACTIVE).build()
    );

    Process proc;

    for (int i = 0; i < processes.size(); i++) {
      proc = processes.get(i);
      
      long configuredTimeout = proc.getPollTimeout() > 0 ? 
          TimeUnit.MILLISECONDS.convert(proc.getPollTimeout(), TimeUnit.SECONDS) : 
          processor.getConfiguration().getProcessTimeoutMillis();
      if ((proc.getStatus() == Process.LifeCycleStatus.ACTIVE) && proc.isTimedOut(configuredTimeout)) {
        
        // if interop is not enabled: proceeding to diagnostic
        if (!proc.isInteropEnabled()) {
          ProcessDiagnosticResult diag = diagnostics.acquireProcessDiagnostics(proc, OptionalValue.of(lockOwner));
          if (diag.getStatus().isFinal() && diag.getStatus().isProblem()) {
            proc.incrementStaleDetectionCount();
          } else {
            // all OK
            // faking poll to clear stale detection count
            proc.poll();
            // moving on to next process
            continue;
          }
        } else {
          proc.incrementStaleDetectionCount();
        }
        
        if (!processorConf.autoRestartStaleProcesses()) {
          ctx.warn(String.format("Stale process detected. Auto-restart disabled (process will not be restarted): %s. Last poll: %s." 
              + " Timeout set to %s millis", 
              proc, new Date(proc.getLastAccess()), configuredTimeout)
          );
          proc.setStatus(LifeCycleStatus.STALE);
          proc.save();
          ctx.getServerContext().getServices().getEventDispatcher().dispatch(new ProcessStaleEvent(proc));
        } else if (proc.getLock().isLocked()) {
          ctx.warn(String.format("Process timed out but locked, probably terminating or restarting: %s", proc));
        } else {
          proc.setStatus(Process.LifeCycleStatus.KILL_REQUESTED);
          proc.save();

          ctx.warn(String.format("Process timed out - ordering kill: %s. Will retry %s time(s). Last poll: %s. Timeout set to %s millis", 
              proc, proc.getMaxKillRetry(),
              new Date(proc.getLastAccess()), configuredTimeout)
          );
          ctx.getTaskManager().executeBackground(
              new KillTask(proc.getMaxKillRetry()),
              TaskParams.createFor(proc, ProcessTerminationRequestor.KILL_REQUESTOR_SERVER),
              BackgroundTaskConfig.create().setExecDelay(processorConf.getKillIntervalMillis())
                  .setExecInterval(processorConf.getKillIntervalMillis()));
        }
      } else {
        ctx.debug(String.format("Process %s is alive. Last poll: %s", 
            proc, new Date(proc.getLastAccess()))
        );
      }
    }

    ctx.debug("Stale process check finished");

    return null;
  }
}
