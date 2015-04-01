package org.sapia.corus.client.cli.command.exec;

import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.console.Option;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.command.Restart;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.corus.client.services.processor.KillPreferences;
import org.sapia.corus.client.services.processor.ProcessCriteria;
import org.sapia.ubik.util.Collects;

/**
 * Implements the logic to restart process by OS pid (
 * <code>restart -op 12345 12346</code>).
 * 
 * @author yduchesne
 * 
 */
public class RestartByOsPidCommand extends RestartAndWaitCommandSupport {

  private static final long PAUSE = 1000;
  
  @Override
  protected void validate(CmdLine cmdLine) throws InputException {
    super.validate(cmdLine);
  }
  
  @Override
  protected void doInit(CliContext context) {
  }
  
  @Override
  public List<OptionDef> getAvailableOptions() {
    return Collects.arrayToList(OPT_OS_PID, OPT_HARD_KILL, OPT_WAIT, OPT_CLUSTER);
  }

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {

    CmdLine cmd = ctx.getCommandLine();
    String osPid = cmd.assertOption(OPT_OS_PID.getName(), true).getValue();
    restartProcessByOsPid(ctx, osPid);

    while (cmd.hasNext()) {
      sleep(PAUSE);
      if (cmd.isNextArg()) {
        Arg argument = cmd.assertNextArg();
        restartProcessByOsPid(ctx, argument.getName());
      } else {
        cmd.next();
      }
    }
  }

  private void restartProcessByOsPid(CliContext ctx, String osPid) throws InputException {
    
    ClusterInfo cluster = getClusterInfo(ctx);
    ProcessCriteria criteria = ProcessCriteria.builder().osPid(ArgMatchers.exact(osPid)).build();
    Option wait = getWaitOption(ctx);
    KillPreferences prefs = KillPreferences.newInstance().setHard(isHardKillOption(ctx));

    if (wait != null) {
      ctx.getConsole().println("Waiting for process restart, please stand by...");
      doRestartAndWait(ctx, cluster, criteria, prefs, wait.getValue() == null ? Restart.DEFAULT_RESTART_WAIT_TIME_SECONDS : wait.asInt());

    } else {
      ctx.getConsole().println("Triggering process restart...");
      ctx.getCorus().getProcessorFacade().restart(criteria, prefs, cluster);
    }
    
    /*
    Process processToRestart = null;
    Results<List<Process>> results = ctx.getCorus().getProcessorFacade().getProcesses(ProcessCriteria.builder().all(), new ClusterInfo(false));
    while (results.hasNext() && processToRestart == null) {
      Result<List<Process>> result = results.next();
      for (Process process : result.getData()) {
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
    }*/
  }

  /*
  private void restartProcess(CliContext ctx, Process aProcess) throws InputException {
    try {
      KillPreferences prefs = KillPreferences.newInstance().setHard(isHardKillOption(ctx));
      ctx.getCorus().getProcessorFacade().restart(aProcess.getProcessID(), prefs);
    } catch (Exception e) {
      throw new InputException(e.getMessage());
    }
    ctx.getConsole().println("Proceeding to restart of process " + aProcess.getProcessID() + "...");
  }*/

}
