package org.sapia.corus.processor.task;

import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskParams;

/**
 * Attempts soft-killing a given process: triggers the sending of the kill command to the process.
 * 
 * @author yduchesne
 */
public class AttemptKillTask extends Task<Boolean, TaskParams<Process, ProcessTerminationRequestor, Integer, Void>>{
  
  /**
   * Returns <code>true</code> if the process has confirmed its shutdown, <code>false</code> otherwise.
   */
  @Override
  public Boolean execute(
      TaskExecutionContext ctx,
      TaskParams<Process, ProcessTerminationRequestor, Integer, Void> params)
      throws Throwable {
    
    Process proc = params.getParam1();
    ProcessTerminationRequestor requestor = params.getParam2();
    int currentRetryCount = params.getParam3().intValue();
    
    if (proc.getStatus() == Process.LifeCycleStatus.KILL_CONFIRMED) {
      ctx.info(String.format("Process %s has confirmed shutdown", proc));
      return true;
    }
    ctx.info(String.format(
        "Attempting to kill process %s. Attempt: %s; requestor %s", 
        proc, 
        currentRetryCount, 
        requestor
    ));
    proc.kill(requestor);
    proc.save();
    return false;
  }

}
