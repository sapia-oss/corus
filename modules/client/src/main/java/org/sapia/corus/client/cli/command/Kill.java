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
import org.sapia.corus.client.services.processor.KillPreferences;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.ubik.util.Collects;

/**
 * Kills running processes.
 * 
 * @author Yanick Duchesne
 */
public class Kill extends CorusCliCommand {

  protected boolean suspend;

  public static final OptionDef WAIT_COMPLETION_OPT = new OptionDef("w", false);
  public static final OptionDef HARD_KILL_OPT       = new OptionDef("hard", false);

  protected static final List<OptionDef> AVAIL_OPTIONS = Collects.arrayToList(
      OPT_PROCESS_ID, OPT_PROCESS_NAME, OPT_DIST, OPT_VERSION, OPT_PROFILE, OPT_OS_PID,
      WAIT_COMPLETION_OPT, HARD_KILL_OPT, OPT_CLUSTER
  );
  
  private static final long DEFAULT_WAIT_COMPLETION_TIMEOUT = 60000;

  protected Kill(boolean suspend) {
    this.suspend = suspend;
  }

  public Kill() {
  }
  
  @Override
  protected void doInit(CliContext context) {
  }
  
  @Override
  public List<OptionDef> getAvailableOptions() {
    return AVAIL_OPTIONS;
  }

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    String dist = null;
    String version = null;
    String profile = null;
    String vmName = null;
    String pid = null;
    String osPid = null;

    CmdLine cmd = ctx.getCommandLine();
    
    KillPreferences prefs = KillPreferences.newInstance();
    prefs.setHard(getOpt(ctx, HARD_KILL_OPT.getName()) != null);

    // Kill ALL
    if (cmd.isNextArg()) {
      cmd.assertNextArg(new String[] { ARG_ALL });
      ProcessCriteria criteria = ProcessCriteria.builder().distribution(WILD_CARD).version(WILD_CARD).build();
      MatchCompletionHook completion = new MatchCompletionHook(criteria);
      ClusterInfo cluster = getClusterInfo(ctx);
      if (suspend) {
        ctx.getCorus().getProcessorFacade().suspend(criteria, prefs, cluster);
      } else {
        ctx.getCorus().getProcessorFacade().kill(criteria, prefs, cluster);
      }
      waitForKillCompletion(ctx, completion);
    }
    // Kill by VM IDENTIDER
    else if (cmd.containsOption(OPT_PROCESS_ID.getName(), true)) {
      PidCompletionHook completion = new PidCompletionHook();
      pid = cmd.assertOption(OPT_PROCESS_ID.getName(), true).getValue();
      completion.addPid(pid);
      killProcessByVmId(ctx, pid);

      while (cmd.hasNext()) {
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
    } else if (cmd.containsOption(OPT_OS_PID.getName(), true)) {
      osPid = cmd.assertOption(OPT_OS_PID.getName(), true).getValue();
      PidCompletionHook completion = new PidCompletionHook();
      pid = killProcessByOsPid(ctx, osPid);
      completion.addPid(pid);

      while (cmd.hasNext()) {
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

      dist = cmd.assertOption(OPT_DIST.getName(), true).getValue();

      version = cmd.assertOption(OPT_VERSION.getName(), true).getValue();

      if (cmd.containsOption(OPT_PROFILE.getName(), true)) {
        profile = cmd.assertOption(OPT_PROFILE.getName(), true).getValue();
      }

      if (cmd.containsOption(OPT_PROCESS_NAME.getName(), true)) {
        vmName = cmd.assertOption(OPT_PROCESS_NAME.getName(), true).getValue();
      }

      ProcessCriteria criteria = ProcessCriteria.builder().name(vmName).profile(profile).distribution(dist).version(version).build();

      ClusterInfo cluster = getClusterInfo(ctx);

      ctx.getConsole().println("Proceeding to kill...");
      MatchCompletionHook completion = new MatchCompletionHook(criteria);
      if (suspend) {
        ctx.getCorus().getProcessorFacade().suspend(criteria, prefs, cluster);
      } else {
        ctx.getCorus().getProcessorFacade().kill(criteria, prefs, cluster);
      }

      waitForKillCompletion(ctx, completion);
    }
  }

  protected void killProcessByVmId(CliContext ctx, String vmId) throws InputException {
    Process processToKill = null;
    Results<List<Process>> results = ctx.getCorus().getProcessorFacade().getProcesses(ProcessCriteria.builder().all(), new ClusterInfo(false));
    while (results.hasNext() && processToKill == null) {
      Result<List<Process>> processes = results.next();
      for (Process process : processes.getData()) {
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
    Results<List<Process>> results = ctx.getCorus().getProcessorFacade().getProcesses(ProcessCriteria.builder().all(), new ClusterInfo(false));
    while (results.hasNext() && processToKill == null) {
      Result<List<Process>> processes = results.next();
      for (Process process : processes.getData()) {
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
    if (suspend) {
      try {
        KillPreferences prefs = KillPreferences.newInstance();
        prefs.setHard(getOpt(ctx, HARD_KILL_OPT.getName()) != null);
        ctx.getCorus().getProcessorFacade().suspend(aProcess.getProcessID(), prefs);
        ctx.getConsole().println("Suspending process " + aProcess.getProcessID() + "...");
      } catch (ProcessNotFoundException e) {
        throw new InputException(e.getMessage());
      }
    } else {
      try {
        KillPreferences prefs = KillPreferences.newInstance();
        prefs.setHard(getOpt(ctx, HARD_KILL_OPT.getName()) != null);
        ctx.getCorus().getProcessorFacade().kill(aProcess.getProcessID(), prefs);
      } catch (ProcessNotFoundException e) {
        throw new InputException(e.getMessage());
      }
      ctx.getConsole().println("Proceeding to kill of process " + aProcess.getProcessID() + "...");
    }
  }

  private void waitForKillCompletion(CliContext ctx, KillCompletionHook hook) throws InputException {
    boolean waitForCompletion = ctx.getCommandLine().containsOption(WAIT_COMPLETION_OPT.getName(), false);
    long timeout = DEFAULT_WAIT_COMPLETION_TIMEOUT;
    if (waitForCompletion) {
      Option opt = ctx.getCommandLine().assertOption(WAIT_COMPLETION_OPT.getName(), false);
      ctx.getConsole().println("Waiting for process termination, please stand by...");
      if (opt.getValue() != null) {
        try {
          timeout = Long.parseLong(opt.getValue()) * 1000;
        } catch (NumberFormatException e) {
        }
      }
    } else {
      return;
    }
    long total = 0;
    while (waitForCompletion) {
      if (hook.isCompleted(ctx)) {
        ctx.getConsole().println("Process(es) terminated.");
        break;
      }
      try {
        long start = System.currentTimeMillis();
        Thread.sleep(2000);
        total += (System.currentTimeMillis() - start);
        if (timeout > 0 && total > timeout) {
          throw new InputException("Process(es) not killed within specified timeout; waiting aborted.");
        }
      } catch (InterruptedException e) {
        break;
      }
    }
  }

  // /////////////////////////// INNER CLASSES ///////////////////////////////

  interface KillCompletionHook {
    boolean isCompleted(CliContext ctx) throws InputException;
  }

  class PidCompletionHook implements KillCompletionHook {

    private List<String> pids = new ArrayList<String>();

    void addPid(String pid) {
      if (pid != null)
        pids.add(pid);
    }

    public boolean isCompleted(CliContext ctx) {
      boolean completed = true;
      for (String vmId : pids) {
        try {
          ctx.getCorus().getProcessorFacade().getProcess(vmId);
        } catch (Exception e) {
          completed = false;
        }
      }
      return completed;
    }
  }

  class MatchCompletionHook implements KillCompletionHook {

    ProcessCriteria criteria;

    public MatchCompletionHook(ProcessCriteria criteria) {
      this.criteria = criteria;
    }

    public boolean isCompleted(CliContext ctx) throws InputException {
      ClusterInfo cluster = getClusterInfo(ctx);
      return isCompleted(ctx.getCorus().getProcessorFacade().getProcesses(criteria, cluster));
    }

    private boolean isCompleted(Results<List<Process>> results) {
      boolean completed = true;
      while (results.hasNext()) {
        Result<List<Process>> result = results.next();
        if (!result.getData().isEmpty()) {
          completed = false;
        }
      }
      return completed;
    }
  }
}
