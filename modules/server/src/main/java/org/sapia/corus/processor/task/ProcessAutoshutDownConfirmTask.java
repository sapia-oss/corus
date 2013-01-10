package org.sapia.corus.processor.task;

import org.sapia.corus.client.services.processor.LockOwner;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;

/**
 * This task initiates process cleanup after an auto-shutdown.
 * 
 * @author yduchesne
 *
 */
public class ProcessAutoshutDownConfirmTask extends Task<Void, org.sapia.corus.client.services.processor.Process> {
  
  @Override
  public Void execute(TaskExecutionContext ctx, org.sapia.corus.client.services.processor.Process process) throws Throwable {

    LockOwner owner = LockOwner.createInstance();
    process.getLock().acquire(owner);
    try {
      process.confirmKilled();
      process.save();
      ctx.getTaskManager().executeAndWait(new CleanupProcessTask(), process).get();
    } finally {
      process.getLock().release(owner);
    }
    return null;
  }

}
