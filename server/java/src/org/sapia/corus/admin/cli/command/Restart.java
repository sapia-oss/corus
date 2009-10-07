package org.sapia.corus.admin.cli.command;

import java.util.Iterator;

import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.corus.ClusterInfo;
import org.sapia.corus.admin.HostList;
import org.sapia.corus.admin.Results;
import org.sapia.corus.admin.cli.CliContext;
import org.sapia.corus.admin.services.processor.Process;
import org.sapia.corus.exceptions.LogicException;


/**
 * @author Yanick Duchesne
 */
public class Restart extends CorusCliCommand {

  @Override
  protected void doExecute(CliContext ctx) throws AbortException,
      InputException {
    
    String  vmId    = null;
    String  osPid   = null; 
    CmdLine cmd = ctx.getCommandLine();

    // restart by VM IDENTIDER
    if (cmd.containsOption(CorusCliCommand.VM_ID_OPT, true)) {
      vmId = cmd.assertOption(CorusCliCommand.VM_ID_OPT, true).getValue();
      restartProcessByVmId(ctx, vmId);

      while (cmd.hasNext()) {
        sleep(1000);
        if (cmd.isNextArg()) {
          Arg argument = cmd.assertNextArg();
          restartProcessByVmId(ctx, argument.getName());
        } else {
          cmd.next();
        }
      }

    // restart by OS PROCESS ID
    } else if (cmd.containsOption(CorusCliCommand.OS_PID_OPT, true)) {
      osPid = cmd.assertOption(CorusCliCommand.OS_PID_OPT, true).getValue();
      restartProcessByOsPid(ctx, osPid);
      
      while (cmd.hasNext()) {
        sleep(1000);
        if (cmd.isNextArg()) {
          Arg argument = cmd.assertNextArg();
          restartProcessByOsPid(ctx, argument.getName());
        } else {
          cmd.next();
        }
      }
      
    }
   
  }
  
  protected void restartProcessByVmId(CliContext ctx, String vmId) throws InputException {
    Process processToRestart = null;
    Results aResult = ctx.getCorus().getProcesses(new ClusterInfo(false));
    while (aResult.hasNext() && processToRestart == null) {
      HostList aList = (HostList) aResult.next();
      
      for (Iterator it = aList.iterator(); it.hasNext() && processToRestart == null; ) {
        Process process = (Process) it.next();
        if (process.getProcessID().equals(vmId)) {
          processToRestart = process;
        }
      }
    }
    
    if (processToRestart != null) {
      restartProcess(ctx, processToRestart);
    } else {
      ctx.getConsole().println("ERROR: Could not restart process, no active process found for the VM id " + vmId);
    }
  }
  
  protected void restartProcessByOsPid(CliContext ctx, String osPid) throws InputException {
    Process processToRestart = null;
    Results aResult = ctx.getCorus().getProcesses(new ClusterInfo(false));
    while (aResult.hasNext() && processToRestart == null) {
      HostList aList = (HostList) aResult.next();
      
      for (Iterator it = aList.iterator(); it.hasNext() && processToRestart == null; ) {
        Process aProcess = (Process) it.next();
        if (aProcess.getOsPid() != null && aProcess.getOsPid().equals(osPid)) {
          String vmId = aProcess.getProcessID();
          processToRestart = aProcess;
          ctx.getConsole().println("Found VM " + vmId + " associated to OS pid " + osPid);
        }
      }
    }

    if (processToRestart != null) {
      restartProcess(ctx, processToRestart);
    } else {
      ctx.getConsole().println("ERROR: Could not restart process, no active process found for OS pid " + osPid);
    }
  }
  
  protected void restartProcess(CliContext ctx, Process aProcess) throws InputException {
    try {
      ctx.getCorus().restart(aProcess.getProcessID());
    } catch (LogicException e) {
      throw new InputException(e.getMessage());
    }

    ctx.getConsole().println("Proceeding to restart of VM " + aProcess.getProcessID() + "...");
  }

}
