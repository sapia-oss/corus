package org.sapia.corus.admin.cli.command;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.corus.ClusterInfo;
import org.sapia.corus.admin.HostItem;
import org.sapia.corus.admin.HostList;
import org.sapia.corus.admin.Results;
import org.sapia.corus.admin.cli.CliContext;
import org.sapia.corus.admin.services.processor.Process;
import org.sapia.corus.exceptions.LogicException;


/**
 * @author Yanick Duchesne
 */
public class Kill extends CorusCliCommand {
  protected boolean _suspend;

  public static final String WAIT_COMPLETION_OPT = "w";
  
  protected Kill(boolean suspend) {
    _suspend = suspend;
  }

  public Kill() {
  }

  @Override
  protected void doExecute(CliContext ctx)
                    throws AbortException, InputException {
    String  dist    = null;
    String  version = null;
    String  profile = null;
    String  vmName  = null;
    String  vmId    = null;
    String  osPid   = null;

    CmdLine cmd = ctx.getCommandLine();

    // Kill by VM IDENTIDER
    if (cmd.containsOption(CorusCliCommand.VM_ID_OPT, true)) {
      VmIdCompletionHook completion = new VmIdCompletionHook();
      vmId = cmd.assertOption(CorusCliCommand.VM_ID_OPT, true).getValue();
      completion.addVmId(vmId);
      killProcessByVmId(ctx, vmId);

      while (cmd.hasNext()) {
        sleep(1000);
        if (cmd.isNextArg()) {
          Arg argument = cmd.assertNextArg();
          vmId = argument.getName();
          completion.addVmId(vmId);
          killProcessByVmId(ctx, vmId);
        } else {
          cmd.next();
        }
      }
      
      waitForKillCompletion(ctx, completion);

    // Kill by OS PROCESS ID
    } else if (cmd.containsOption(CorusCliCommand.OS_PID_OPT, true)) {
      osPid = cmd.assertOption(CorusCliCommand.OS_PID_OPT, true).getValue();
      VmIdCompletionHook completion = new VmIdCompletionHook();
      vmId = killProcessByOsPid(ctx, osPid);
      completion.addVmId(vmId);

      while (cmd.hasNext()) {
        sleep(1000);
        if (cmd.isNextArg()) {
          Arg argument = cmd.assertNextArg();
          vmId = killProcessByOsPid(ctx, argument.getName());
          completion.addVmId(vmId);
        } else {
          cmd.next();
        }
      }
      
      waitForKillCompletion(ctx, completion);
      
    // KILL BY DISTRIBUTION ATTIRBUTES
    } else {
      
      dist = cmd.assertOption(CorusCliCommand.DIST_OPT, true).getValue();
  
      version = cmd.assertOption(CorusCliCommand.VERSION_OPT, true).getValue();
  
      if(cmd.containsOption(CorusCliCommand.PROFILE_OPT, true)){
        profile = cmd.assertOption(CorusCliCommand.PROFILE_OPT, true).getValue();
      }
      
      if (cmd.containsOption(CorusCliCommand.VM_NAME_OPT, true)) {
        vmName = cmd.assertOption(CorusCliCommand.VM_NAME_OPT, true).getValue();
      }
  
      ClusterInfo cluster = getClusterInfo(ctx);

      ctx.getConsole().println("Proceeding to kill...");
      MatchCompletionHook completion = new MatchCompletionHook(dist, version, profile, vmName);
      if (vmName != null) {
        if (_suspend) {
          ctx.getCorus().suspend(dist, version, profile, vmName, cluster);
        } else {
          ctx.getCorus().kill(dist, version, profile, vmName, cluster);
        }
      } else {
        if (_suspend) {
          ctx.getCorus().suspend(dist, version, profile, cluster);
        } else {
          ctx.getCorus().kill(dist, version, profile, cluster);
        }
      }
      waitForKillCompletion(ctx, completion);
    }
  }
  
  protected void killProcessByVmId(CliContext ctx, String vmId) throws InputException {
    Process processToKill = null;
    Results aResult = ctx.getCorus().getProcesses(new ClusterInfo(false));
    while (aResult.hasNext() && processToKill == null) {
      HostList aList = (HostList) aResult.next();
      
      for (Iterator it = aList.iterator(); it.hasNext() && processToKill == null; ) {
        Process process = (Process) it.next();
        if (process.getProcessID().equals(vmId)) {
          processToKill = process;
        }
      }
    }
    
    if (processToKill != null) {
      killProcess(ctx, processToKill);
    } else {
      ctx.getConsole().println("ERROR: Could not kill process, no active process found for the VM id " + vmId);
    }
  }
  
  protected String killProcessByOsPid(CliContext ctx, String osPid) throws InputException {
    Process processToKill = null;
    Results aResult = ctx.getCorus().getProcesses(new ClusterInfo(false));
    while (aResult.hasNext() && processToKill == null) {
      HostList aList = (HostList) aResult.next();
      
      for (Iterator it = aList.iterator(); it.hasNext() && processToKill == null; ) {
        Process aProcess = (Process) it.next();
        if (aProcess.getOsPid() != null && aProcess.getOsPid().equals(osPid)) {
          String vmId = aProcess.getProcessID();
          processToKill = aProcess;
          ctx.getConsole().println("Found VM " + vmId + " associated to OS pid " + osPid);
        }
      }
    }

    if (processToKill != null) {
      killProcess(ctx, processToKill);
      return processToKill.getProcessID();
    } else {
      ctx.getConsole().println("ERROR: Could not kill process, no active process found for OS pid " + osPid);
      return null;
    }
  }
  
  protected void killProcess(CliContext ctx, Process aProcess) throws InputException {
    if (_suspend) {
      ctx.getCorus().suspend(aProcess.getProcessID());
      ctx.getConsole().println("Suspending VM " + aProcess.getProcessID() + "...");
    } else {
      try {
        ctx.getCorus().kill(aProcess.getProcessID());
      } catch (LogicException e) {
        throw new InputException(e.getMessage());
      }
      ctx.getConsole().println("Proceeding to kill of VM " + aProcess.getProcessID() + "...");
    }
  }
  
  private void waitForKillCompletion(CliContext ctx, KillCompletionHook hook){
    boolean waitForCompletion = ctx.getCommandLine().containsOption(WAIT_COMPLETION_OPT, false);
    if(waitForCompletion){
      ctx.getConsole().println("(Waiting for process termination, please stand by)");
    }
    while(waitForCompletion){
      if(hook.isCompleted(ctx)){
        break;
      }
      try{
        Thread.sleep(2000);
      }catch(InterruptedException e){
        break;
      }
    }
  }

  ///////////////////////////// INNER CLASSES ///////////////////////////////

  interface KillCompletionHook{
    boolean isCompleted(CliContext ctx);
  }
  
  class VmIdCompletionHook implements KillCompletionHook{
    
    private List<String> vmIds = new ArrayList<String>();

    void addVmId(String vmId){
      if(vmId != null)vmIds.add(vmId);
    }
    
    public boolean isCompleted(CliContext ctx) {
      boolean completed = true;
      for(String vmId:vmIds){
        try{
          ctx.getCorus().getProcess(vmId);
        }catch(LogicException e){
          completed = false;
        }
      }
      return completed;
    }
  }
  
  class MatchCompletionHook implements KillCompletionHook{
    String dist, version, profile, process;

    public MatchCompletionHook(String dist, String version, String profile, String process) {
      this.dist = dist;
      this.version = version;
      this.process = process;
      this.profile = profile;
    }
    
    public boolean isCompleted(CliContext ctx) {
      ClusterInfo cluster = getClusterInfo(ctx);
      boolean completed = true;
      if(process != null && profile != null){
        completed = isCompleted(ctx.getCorus().getProcesses(dist, version, profile, process, cluster));
      }
      else if(process != null){
        completed = isCompleted(ctx.getCorus().getProcesses(dist, version, process, cluster));
      }
      else{
        completed = isCompleted(ctx.getCorus().getProcesses(dist, version, cluster));
      }
      return completed;
    }
    
    private boolean isCompleted(Results results){
      boolean completed = true;
      while(results.hasNext()){
        Object result = results.next();
        if(result instanceof HostItem){
          completed = false;
        }
        else{
          HostList hl = (HostList)result;
          if(!hl.isEmpty()){
            completed = false;
          }
        }
      }
      return completed;
    }
  }
}
