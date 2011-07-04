package org.sapia.corus.client.cli.command;

import java.util.ArrayList;
import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.console.Option;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.services.processor.Process;


/**
 * @author Yanick Duchesne
 */
public class Kill extends CorusCliCommand {
  protected boolean _suspend;

  public static final String WAIT_COMPLETION_OPT = "w";
  
  private static final long DEFAULT_WAIT_COMPLETION_TIMEOUT = 60000;
  
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
    String  pid     = null;
    String  osPid   = null;

    CmdLine cmd = ctx.getCommandLine();
    
    // Kill ALL
    if(cmd.hasNext() && cmd.isNextArg()){
      cmd.assertNextArg(new String[]{ARG_ALL});
      MatchCompletionHook completion = new MatchCompletionHook(WILD_CARD, WILD_CARD, null, null);
      ClusterInfo cluster = getClusterInfo(ctx);
      if (_suspend) {
        ctx.getCorus().getProcessorFacade().suspend(WILD_CARD, WILD_CARD, null, cluster);
      } else {
        ctx.getCorus().getProcessorFacade().kill(WILD_CARD, WILD_CARD, null, cluster);
      }
      waitForKillCompletion(ctx, completion);
    }
    // Kill by VM IDENTIDER
    else if (cmd.containsOption(VM_ID_OPT, true)) {
      PidCompletionHook completion = new PidCompletionHook();
      pid = cmd.assertOption(VM_ID_OPT, true).getValue();
      completion.addPid(pid);
      killProcessByVmId(ctx, pid);

      while (cmd.hasNext()) {
        sleep(1000);
        if (cmd.isNextArg()) {
          Arg argument = cmd.assertNextArg();
          pid = argument.getName();
          completion.addPid(pid);
          killProcessByVmId(ctx, pid);
        } else {
          cmd.next();
        }
      }
      
      waitForKillCompletion(ctx, completion);

    // Kill by OS PROCESS ID
    } else if (cmd.containsOption(OS_PID_OPT, true)) {
      osPid = cmd.assertOption(OS_PID_OPT, true).getValue();
      PidCompletionHook completion = new PidCompletionHook();
      pid = killProcessByOsPid(ctx, osPid);
      completion.addPid(pid);

      while (cmd.hasNext()) {
        sleep(1000);
        if (cmd.isNextArg()) {
          Arg argument = cmd.assertNextArg();
          pid = killProcessByOsPid(ctx, argument.getName());
          completion.addPid(pid);
        } else {
          cmd.next();
        }
      }
      
      waitForKillCompletion(ctx, completion);
      
    // KILL BY DISTRIBUTION ATTIRBUTES
    } else {
      
      dist = cmd.assertOption(DIST_OPT, true).getValue();
  
      version = cmd.assertOption(VERSION_OPT, true).getValue();
  
      if(cmd.containsOption(PROFILE_OPT, true)){
        profile = cmd.assertOption(PROFILE_OPT, true).getValue();
      }
      
      if (cmd.containsOption(VM_NAME_OPT, true)) {
        vmName = cmd.assertOption(VM_NAME_OPT, true).getValue();
      }
  
      ClusterInfo cluster = getClusterInfo(ctx);

      ctx.getConsole().println("Proceeding to kill...");
      MatchCompletionHook completion = new MatchCompletionHook(dist, version, profile, vmName);
      if (vmName != null) {
        if (_suspend) {
          ctx.getCorus().getProcessorFacade().suspend(dist, version, profile, vmName, cluster);
        } else {
          ctx.getCorus().getProcessorFacade().kill(dist, version, profile, vmName, cluster);
        }
      } else {
        if (_suspend) {
          ctx.getCorus().getProcessorFacade().suspend(dist, version, profile, cluster);
        } else {
          ctx.getCorus().getProcessorFacade().kill(dist, version, profile, cluster);
        }
      }
      waitForKillCompletion(ctx, completion);
    }
  }
  
  protected void killProcessByVmId(CliContext ctx, String vmId) throws InputException {
    Process processToKill = null;
    Results<List<Process>> results = ctx.getCorus().getProcessorFacade().getProcesses(new ClusterInfo(false));
    while (results.hasNext() && processToKill == null) {
      Result<List<Process>> processes = results.next();
      for(Process process:processes.getData()){
        if (process.getProcessID().equals(vmId)) {
          processToKill = process;
          break;
        }
      }
    }
    
    if (processToKill != null) {
      killProcess(ctx, processToKill);
    } else {
      throw new InputException("Could not kill process, no active process found for the process id " + vmId);
    }
  }
  
  protected String killProcessByOsPid(CliContext ctx, String osPid) throws InputException {
    Process processToKill = null;
    Results<List<Process>> results = ctx.getCorus().getProcessorFacade().getProcesses(new ClusterInfo(false));
    while (results.hasNext() && processToKill == null) {
      Result<List<Process>> processes = results.next();
      for(Process process:processes.getData()){
        if (process.getOsPid() != null && process.getOsPid().equals(osPid)) {
          String pid = process.getProcessID();
          processToKill = process;
          ctx.getConsole().println("Found process " + pid + " associated to OS pid " + osPid);
          break;
        }
      }
    }

    if (processToKill != null) {
      killProcess(ctx, processToKill);
      return processToKill.getProcessID();
    } else {
      throw new InputException("Could not kill process, no active process found for OS pid " + osPid);
    }
  }
  
  protected void killProcess(CliContext ctx, Process aProcess) throws InputException {
    if (_suspend) {
      try{
        ctx.getCorus().getProcessorFacade().suspend(aProcess.getProcessID());
        ctx.getConsole().println("Suspending process " + aProcess.getProcessID() + "...");
      }catch(ProcessNotFoundException e){
        throw new InputException(e.getMessage());
      }
    } else {
      try {
        ctx.getCorus().getProcessorFacade().kill(aProcess.getProcessID());
      } catch (ProcessNotFoundException e) {
        throw new InputException(e.getMessage());
      }
      ctx.getConsole().println("Proceeding to kill of process " + aProcess.getProcessID() + "...");
    }
  }
  
  private void waitForKillCompletion(CliContext ctx, KillCompletionHook hook) throws InputException{
    boolean waitForCompletion = ctx.getCommandLine().containsOption(WAIT_COMPLETION_OPT, false);
    long timeout = DEFAULT_WAIT_COMPLETION_TIMEOUT;
    if(waitForCompletion){
      Option opt = ctx.getCommandLine().assertOption(WAIT_COMPLETION_OPT, false);
      ctx.getConsole().println("Waiting for process termination, please stand by...");
      if(opt.getValue() != null){
        try{
          timeout = Long.parseLong(opt.getValue()) * 1000;
        }catch(NumberFormatException e){}
      }
    }
    else{
      return;
    }
    long total = 0;
    while(waitForCompletion){
      if(hook.isCompleted(ctx)){
        ctx.getConsole().println("Process(es) terminated.");
        break;
      }
      try{
        long start = System.currentTimeMillis();
        Thread.sleep(2000);
        total += (System.currentTimeMillis() - start);
        if(timeout > 0 && total > timeout){
          throw new InputException("Process(es) not killed within specified timeout; waiting aborted.");
        }
      }catch(InterruptedException e){
        break;
      }
    }
  }

  ///////////////////////////// INNER CLASSES ///////////////////////////////

  interface KillCompletionHook{
    boolean isCompleted(CliContext ctx);
  }
  
  class PidCompletionHook implements KillCompletionHook{
    
    private List<String> pids = new ArrayList<String>();

    void addPid(String pid){
      if(pid != null)pids.add(pid);
    }
    
    public boolean isCompleted(CliContext ctx) {
      boolean completed = true;
      for(String vmId:pids){
        try{
          ctx.getCorus().getProcessorFacade().getProcess(vmId);
        }catch(Exception e){
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
      if(process != null){
        completed = isCompleted(ctx.getCorus().getProcessorFacade().getProcesses(dist, version, profile, process, cluster));
      }
      else{
        completed = isCompleted(ctx.getCorus().getProcessorFacade().getProcesses(dist, version, cluster));
      }
      return completed;
    }
    
    private boolean isCompleted(Results<List<Process>> results){
      boolean completed = true;
      while(results.hasNext()){
        Result<List<Process>> result = results.next();
        if(!result.getData().isEmpty()){
          completed = false;
        }
      }
      return completed;
    }
  }
}
