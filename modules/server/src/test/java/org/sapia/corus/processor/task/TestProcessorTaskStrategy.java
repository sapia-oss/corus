package org.sapia.corus.processor.task;

import java.io.File;
import java.io.IOException;

import org.sapia.console.CmdLine;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;

public class TestProcessorTaskStrategy extends ProcessorTaskStrategyImpl{
  
  boolean nativeKill, noPid, restart, restartInvalid;
  
  @Override
  public boolean execCmdLine(TaskExecutionContext ctx, File processDir,
      CmdLine cmdLine, Process process) {
    return true;
  }

  @Override
  protected void doNativeKill(TaskExecutionContext ctx, Process proc)
      throws IOException {
    nativeKill = true;
  }
 
  @Override
  protected void onNoOsPid() {
    noPid = true;
  }
  
  @Override
  protected void onRestarted() {
    restart = true;
  }
  
  @Override
  protected void onRestartThresholdInvalid() {
    restartInvalid = true;
  }
}
