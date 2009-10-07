package org.sapia.corus.processor.task;

import java.io.File;
import java.util.Properties;

import org.sapia.console.CmdLine;
import org.sapia.corus.admin.services.processor.Process;
import org.sapia.corus.admin.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.processor.ProcessInfo;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;

public interface ProcessorTaskStrategy {
  
  public boolean attemptKill(
      TaskExecutionContext ctx, 
      ProcessTerminationRequestor requestor, 
      Process proc, 
      int currentRetryCount) throws Throwable;
  
  public void cleanupProcess(TaskExecutionContext ctx, Process proc) throws Throwable;

  public boolean execCmdLine(
      TaskExecutionContext ctx, 
      File processDir, 
      CmdLine cmdLine, 
      Process proc) throws Throwable;
  
  public boolean execProcess(
      TaskExecutionContext ctx, 
      ProcessInfo info, 
      Properties processProperties) throws Throwable;
  
  public boolean forcefulKill(
      TaskExecutionContext ctx, 
      ProcessTerminationRequestor requestor, 
      String corusPid) throws Throwable;
  
  public void killConfirmed(
      TaskExecutionContext ctx, 
      Process process) throws Throwable;
  
  public void killProcess(
      TaskExecutionContext ctx, 
      ProcessTerminationRequestor requestor, 
      Process proc) throws Throwable;
  
  public File makeProcessDir(
      TaskExecutionContext ctx, 
      ProcessInfo info) throws Throwable;
  
  public boolean restartProcess(TaskExecutionContext ctx, Process process) throws Throwable;
}
