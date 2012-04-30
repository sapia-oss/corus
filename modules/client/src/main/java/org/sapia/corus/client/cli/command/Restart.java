package org.sapia.corus.client.cli.command;

import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;

/**
 * @author Yanick Duchesne
 */
public class Restart extends CorusCliCommand {

  @Override
  protected void doExecute(CliContext ctx) throws AbortException,
      InputException {
    
    String  pid    = null;
    String  osPid   = null; 
    CmdLine cmd = ctx.getCommandLine();

    // restart ALL
    if(cmd.hasNext() && cmd.isNextArg()){
      cmd.assertNextArg(new String[]{ARG_ALL});
      ClusterInfo cluster = getClusterInfo(ctx);
      
      ctx.getConsole().println("Proceeding to restart...");
      
      ProcessCriteria criteria = ProcessCriteria.builder().all();
      ctx.getCorus().getProcessorFacade().restart(criteria, cluster);
    }     
    // restart by process IDENTIDER
    else if (cmd.containsOption(VM_ID_OPT, true)) {
      pid = cmd.assertOption(VM_ID_OPT, true).getValue();
      restartProcessByVmId(ctx, pid);

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
    } else if (cmd.containsOption(OS_PID_OPT, true)) {
      osPid = cmd.assertOption(OS_PID_OPT, true).getValue();
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
      
    } else {
      String distName     = cmd.assertOption(DIST_OPT, true).getValue();
      String version      = cmd.assertOption(VERSION_OPT, true).getValue();
      String processName  = cmd.containsOption(VM_NAME_OPT, false) ? cmd.assertOption(VM_NAME_OPT, true).getValue() : null;
      String profile      = cmd.containsOption(PROFILE_OPT, false) ? cmd.assertOption(PROFILE_OPT, true).getValue() : null;
      

      ProcessCriteria criteria = ProcessCriteria.builder()
        .name(processName)
        .distribution(distName)
        .version(version)
        .profile(profile)
        .build();
      
      ctx.getConsole().println("Proceeding to restart...");      
      ctx.getCorus().getProcessorFacade().restart(criteria, getClusterInfo(ctx));
    }
   
  }
  
  protected void restartProcessByVmId(CliContext ctx, String pid) throws InputException {
    Process processToRestart = null;
    Results<List<Process>> results = ctx.getCorus().getProcessorFacade().getProcesses(
        ProcessCriteria.builder().all(), 
        new ClusterInfo(false)
    );
    while (results.hasNext() && processToRestart == null) {
      Result<List<Process>> result = results.next();
      for(Process process:result.getData()){
        if (process.getProcessID().equals(pid)) {
          processToRestart = process;
        }
      }
    }
    
    if (processToRestart != null) {
      restartProcess(ctx, processToRestart);
    } else {
      throw new InputException("Could not restart process, no active process found for the process ID " + pid);
    }
  }
  
  protected void restartProcessByOsPid(CliContext ctx, String osPid) throws InputException {
    Process processToRestart = null;
    Results<List<Process>> results = ctx.getCorus().getProcessorFacade().getProcesses(
        ProcessCriteria.builder().all(), 
        new ClusterInfo(false)
    );
    while (results.hasNext() && processToRestart == null) {
      Result<List<Process>> result = results.next();
      for(Process process:result.getData()){
        if (process.getOsPid() != null && process.getOsPid().equals(osPid)) {
          String pid = process.getProcessID();
          processToRestart = process;
          ctx.getConsole().println("Found process " + pid + " associated to OS pid " + osPid);
        }
      }
    }

    if (processToRestart != null) {
      restartProcess(ctx, processToRestart);
    } else {
      throw new InputException("Could not restart process, no active process found for OS pid " + osPid);
    }
  }
  
  protected void restartProcess(CliContext ctx, Process aProcess) throws InputException {
    try {
      ctx.getCorus().getProcessorFacade().restart(aProcess.getProcessID());
    } catch (Exception e) {
      throw new InputException(e.getMessage());
    }

    ctx.getConsole().println("Proceeding to restart of process " + aProcess.getProcessID() + "...");
  }

}
