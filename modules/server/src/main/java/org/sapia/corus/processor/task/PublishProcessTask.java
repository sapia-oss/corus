package org.sapia.corus.processor.task;

import org.sapia.corus.client.common.OptionalValue;
import org.sapia.corus.client.common.ToStringUtils;
import org.sapia.corus.client.exceptions.processor.ProcessLockException;
import org.sapia.corus.client.services.diagnostic.DiagnosticModule;
import org.sapia.corus.client.services.diagnostic.ProcessDiagnosticResult;
import org.sapia.corus.client.services.processor.LockOwner;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.event.ProcessPublishingCompletedEvent;
import org.sapia.corus.client.services.processor.event.ProcessPublishingCompletedEvent.PublishStatus;
import org.sapia.corus.client.services.processor.event.ProcessPublishingPendingEvent;
import org.sapia.corus.client.services.pub.ProcessPubContext;
import org.sapia.corus.client.services.pub.ProcessPublisher;
import org.sapia.corus.client.services.pub.PublishingCallback;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;

/**
 * Publishes the given process, after having proceeded to its successful diagnostic.
 * 
 * @author yduchesne
 *
 */
public class PublishProcessTask extends Task<Void, Process> {
 
  private LockOwner                  processLockOwner = new LockOwner();
  private PublishProcessTaskCallback callback;
  private Process                    process;
  
  /**
   * @param maxRetries an optional number of retries.
   */
  public PublishProcessTask(OptionalValue<Integer> maxRetries) {
    if (maxRetries.isSet()) {
      super.setMaxExecution(maxRetries.get());
    }
  }
  
  @Override
  public Void execute(TaskExecutionContext ctx, Process toPublish) throws Throwable {
    
    process = toPublish;
    
    if (getExecutionCount() == 0) {
      ctx.getServerContext().getServices().getEventDispatcher().dispatch(new ProcessPublishingPendingEvent(process));
    }
    
    if (process.getLock().isLocked() && !process.getLock().getOwner().equals(processLockOwner)) {
      ctx.warn(String.format("Process currently locked, will try again: %s", ToStringUtils.toString(process)));
    } else {
      try {
        process.getLock().acquire(processLockOwner);
      } catch (ProcessLockException ple) {
        ctx.warn(String.format("Could not acquire lock on process: %s. Will try again", ToStringUtils.toString(process)));
        ctx.warn("Details:", ple);
        return null;        
      } 
      
      DiagnosticModule diag      = ctx.getServerContext().getServices().getDiagnosticModule();
      ProcessPublisher publisher = ctx.getServerContext().getServices().getProcessPublisher();
      
      ProcessDiagnosticResult diagnosticResult = diag.acquireDiagnosticFor(process, processLockOwner);
      
      if (diagnosticResult.getStatus().isFinal() && !diagnosticResult.getStatus().isProblem()) {
        if (callback == null) {
          callback = new PublishProcessTaskCallback(ctx);
          publisher.publishProcess(process, callback);
          abort(ctx);
        }
      } else if (diagnosticResult.getStatus().isFinal() && diagnosticResult.getStatus().isProblem()){
        ctx.error(String.format("Diagnostic failed for process %s (got status: %s). Aborting publishing", 
            ToStringUtils.toString(process), diagnosticResult.getStatus()));
        abort(ctx);
      } else {
        ctx.info(String.format("Diagnostic incomplete for process %s (got status: %s). Will try again.", 
            ToStringUtils.toString(process), diagnosticResult.getStatus()));
      }
    }
      
    return null;
  }
  
  @Override
  protected synchronized void abort(TaskExecutionContext ctx) {
    try {
      ctx.debug(String.format("Releasing lock on: %s", ToStringUtils.toString(process)));
      process.getLock().release(processLockOwner);
    } catch (Exception err) {
      // noop
    } finally {
      super.abort(ctx);
    }
  }
  
  @Override
  protected void onMaxExecutionReached(TaskExecutionContext ctx)
      throws Throwable {
    ctx.getServerContext().getServices().getEventDispatcher().dispatch(
        new ProcessPublishingCompletedEvent(process, PublishStatus.MAX_ATTEMPTS_REACHED)
    );
    ctx.error("Max execution reached when trying to publish process " + ToStringUtils.toString(process));
    abort(ctx);
  }

  // ==========================================================================
  
  private class PublishProcessTaskCallback implements PublishingCallback {
    
    private TaskExecutionContext     taskContext;
    
    private PublishProcessTaskCallback(TaskExecutionContext taskContext) {
      this.taskContext = taskContext;
    }
    
    @Override
    public void publishingFailed(ProcessPubContext ctx, Exception err) {
      taskContext.error("Error occurred trying to publish process " + ToStringUtils.toString(process), err);
      taskContext.getServerContext().getServices().getEventDispatcher().dispatch(new ProcessPublishingCompletedEvent(ctx.getProcess(), err));
    }
    @Override
    public void publishingSuccessful(ProcessPubContext ctx) {
      taskContext.info(String.format("Publishing successful for process %s with publishing config: %s", 
          ToStringUtils.toString(ctx.getProcess()), ctx.getPubConfig()));
      taskContext.getServerContext().getServices().getEventDispatcher().dispatch(new ProcessPublishingCompletedEvent(ctx.getProcess(), PublishStatus.SUCCESS));
    }
    @Override
    public void publishingNotApplicable(Process process) {      
      taskContext.info(String.format("Publishing not applicable for process %s", 
        ToStringUtils.toString(process)));
      taskContext.getServerContext().getServices().getEventDispatcher().dispatch(new ProcessPublishingCompletedEvent(process, PublishStatus.NOT_APPLICABLE));
    }
    
    @Override
    public void publishingStarted(ProcessPubContext ctx) {
      taskContext.info(String.format("Attempting to publish process: %s for %s", ToStringUtils.toString(ctx.getProcess()), ctx.getPubConfig()));
    }
  }
}
