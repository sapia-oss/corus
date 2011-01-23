package org.sapia.corus.processor.task;

import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;


/**
 * @author Yanick Duchesne
 */
public class TestProcessTerminationTask extends ProcessTerminationTask {
  int onExec     = 0;
  int onMaxRetry = 0;

  public TestProcessTerminationTask(ProcessTerminationRequestor requestor, 
                                    String corusPid, 
                                    int maxRetry) throws Exception{
    super(requestor, corusPid, maxRetry);
  }
  
  protected void cleanupProcess(Process proc, TaskExecutionContext ctx) {
  }
  

  @Override
  protected void onExec(TaskExecutionContext ctx) {
    onExec++;
  }

  @Override
  protected void onMaxExecutionReached(TaskExecutionContext ctx)
      throws Throwable {
    onMaxRetry++;
  }
  
  @Override
  protected void onKillConfirmed(TaskExecutionContext ctx) {}
}
