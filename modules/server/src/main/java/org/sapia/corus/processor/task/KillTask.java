package org.sapia.corus.processor.task;

import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.client.services.processor.ProcessorConfiguration;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;

/**
 * This task insures the destruction of a given process.
 *
 * @author Yanick Duchesne
 */
public class KillTask extends ProcessTerminationTask {
  
  /**
   * Constructs an instance of this class with the given params.
   */
  public KillTask(ProcessTerminationRequestor requestor, 
                  String corusPid,
                  int maxRetry) {
    super(requestor, corusPid, maxRetry);
  }

  @Override
  protected void onKillConfirmed(TaskExecutionContext ctx) throws Throwable{
    try {
      ProcessorConfiguration procConfig = ctx.getServerContext().getServices().lookup(Processor.class).getConfiguration();
      Process process = ctx.getServerContext().getServices().getProcesses().getActiveProcesses().getProcess(corusPid());
      ProcessorTaskStrategy strategy = ctx.getServerContext().lookup(ProcessorTaskStrategy.class);
      ctx.info("Process kill confirmed: " + process.getProcessID() + "; removing from active process list; requestor = " + requestor());
      strategy.cleanupProcess(ctx, process);
      if (requestor() == ProcessTerminationRequestor.KILL_REQUESTOR_SERVER) {
        if ((System.currentTimeMillis() - process.getCreationTime()) > procConfig.getRestartIntervalMillis()) {
          ctx.warn("Restarting process: " + process);
          strategy.restartProcess(ctx, process);
          onRestarted(ctx);
        } else {
          ctx.debug("Restarting interval (millis): " + procConfig.getRestartIntervalMillis());
          ctx.warn("Process will not be restarted; not enough time since last restart");
        }
      } else {
        ctx.warn("Process " + corusPid() + " terminated");
      }
    } catch (ProcessNotFoundException e) {
      ctx.error(e.getMessage());
    } finally {
      super.abort(ctx);
    }
  }
  
  @Override
  protected void onMaxExecutionReached(TaskExecutionContext ctx) throws Throwable{
    ProcessorTaskStrategy strategy = ctx.getServerContext().lookup(ProcessorTaskStrategy.class);
    strategy.forcefulKill(ctx, requestor(), corusPid());
    Process process = ctx.getServerContext().getServices().getProcesses().getActiveProcesses().getProcess(corusPid());    
    strategy.cleanupProcess(ctx, process);
  }

  @Override
  protected void onExec(TaskExecutionContext ctx) throws Throwable{
    try {
      Process process = ctx.getServerContext().getServices().getProcesses().getActiveProcesses().getProcess(corusPid());
      ProcessorTaskStrategy strategy = ctx.getServerContext().lookup(ProcessorTaskStrategy.class);
      if(strategy.attemptKill(ctx, requestor(), process, getExecutionCount())){
        abort(ctx);
      }
    } catch (ProcessNotFoundException e) {
      // no Vm for ID...
      abort(ctx);
      ctx.error(e);
    }
  }
  
  protected void onRestarted(TaskExecutionContext ctx){}
}
