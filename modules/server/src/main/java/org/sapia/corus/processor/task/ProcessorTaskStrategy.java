package org.sapia.corus.processor.task;

import java.io.File;
import java.util.Properties;

import org.sapia.console.CmdLine;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.processor.ProcessInfo;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;

public interface ProcessorTaskStrategy {
  
  public boolean attemptKill(
      TaskExecutionContext ctx, 
      ProcessTerminationRequestor requestor, 
      Process proc, 
      int currentRetryCount);
  
  public void cleanupProcess(TaskExecutionContext ctx, Process proc);

  public boolean execCmdLine(
      TaskExecutionContext ctx, 
      File processDir, 
      CmdLine cmdLine, 
      Process proc);
  
  public boolean execProcess(
      TaskExecutionContext ctx, 
      ProcessInfo info, 
      Properties processProperties);
  
  public boolean forcefulKill(
      TaskExecutionContext ctx, 
      ProcessTerminationRequestor requestor, 
      String corusPid);
  
  public void killConfirmed(
      TaskExecutionContext ctx, 
      Process process);
  
  public void killProcess(
      TaskExecutionContext ctx, 
      ProcessTerminationRequestor requestor, 
      Process proc);
  
  public File makeProcessDir(
      TaskExecutionContext ctx, 
      ProcessInfo info);
  
  public boolean restartProcess(TaskExecutionContext ctx, Process process);
}
