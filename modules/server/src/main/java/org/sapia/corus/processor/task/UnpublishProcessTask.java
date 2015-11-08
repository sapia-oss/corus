package org.sapia.corus.processor.task;

import org.sapia.corus.client.common.ToStringUtils;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.event.ProcessUnpublishingCompletedEvent;
import org.sapia.corus.client.services.processor.event.ProcessUnpublishingCompletedEvent.UnpublishStatus;
import org.sapia.corus.client.services.processor.event.ProcessUnpublishingPendingEvent;
import org.sapia.corus.client.services.pub.ProcessPubContext;
import org.sapia.corus.client.services.pub.ProcessPublisher;
import org.sapia.corus.client.services.pub.UnpublishingCallback;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;

/**
 * This task is mean to invoke the unpublishing of a given process, prior to killing it, or 
 * in any other appropriate circumstance.
 * 
 * @author yduchesne
 *
 */
public class UnpublishProcessTask extends Task<Boolean, Process> {
  
  @Override
  public Boolean execute(TaskExecutionContext ctx, Process process) throws Throwable {
    ProcessPublisher      publisher = ctx.getServerContext().getServices().getProcessPublisher();
    UnpublishTaskCallback callback  = new UnpublishTaskCallback(ctx);
    
    ctx.getServerContext().getServices().getEventDispatcher().dispatch(new ProcessUnpublishingPendingEvent(process));
    
    publisher.unpublishProcess(process, callback);
    if (callback.status == UnpublishStatus.FAILURE) {
      return false;
    }
    return true;
  }
  
  // ========================================================================--
  
  class UnpublishTaskCallback implements UnpublishingCallback {
    
    private UnpublishStatus      status;
    private TaskExecutionContext taskContext;
    
    UnpublishTaskCallback(TaskExecutionContext taskContext) {
      this.taskContext = taskContext;
    }
    
    @Override
    public void unpublishingFailed(ProcessPubContext ctx, Exception err) {
      status = UnpublishStatus.FAILURE;
      taskContext.warn("Error occurred trying to unpublish process " + ToStringUtils.toString(ctx.getProcess()), err);
      taskContext.getServerContext().getServices().getEventDispatcher().dispatch(new ProcessUnpublishingCompletedEvent(ctx.getProcess(), err));
    }
   
    @Override
    public void unpublishingNotApplicable(Process process) {
      status = UnpublishStatus.NOT_APPLICABLE;
      taskContext.info(String.format("Unpublishing not applicable for process %s", 
          ToStringUtils.toString(process)));
      taskContext.getServerContext().getServices().getEventDispatcher().dispatch(new ProcessUnpublishingCompletedEvent(process, status));
    }
  
    @Override
    public void unpublishingStarted(ProcessPubContext ctx) {
      taskContext.info(String.format("Attempting to unpublish process: %s for %s", ToStringUtils.toString(ctx.getProcess()), ctx.getPubConfig()));
      taskContext.getServerContext().getServices().getEventDispatcher().dispatch(new ProcessUnpublishingPendingEvent(ctx.getProcess()));
    }
    
    @Override
    public void unpublishingSuccessful(ProcessPubContext ctx) {
      status = UnpublishStatus.SUCCESS;
      taskContext.info(String.format("Unpublishing successful for process %s with publishing config: %s", 
          ToStringUtils.toString(ctx.getProcess()), ctx.getPubConfig()));
      taskContext.getServerContext().getServices().getEventDispatcher().dispatch(new ProcessUnpublishingCompletedEvent(ctx.getProcess(), status));
    }
  }

}
