package org.sapia.corus.client.cli.command.exec;

import java.util.ArrayList;
import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.corus.client.ClusterInfo;
import org.sapia.corus.client.Result;
import org.sapia.corus.client.Results;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.command.AbstractExecCommand;
import org.sapia.corus.client.services.processor.KillPreferences;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;

/**
 * Restarts processes by JVM identifier (<code>restart -i 12345 12346</code>).
 * 
 * @author yduchesne
 * 
 */
public class RestartByVmIdCommand extends AbstractExecCommand {

  private static final long PAUSE = 1000;
  
  @Override
  protected void doInit(CliContext context) {
  }
  
  @Override
  protected List<OptionDef> getAvailableOptions() {
    return new ArrayList<OptionDef>();
  }

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {

    CmdLine cmd = ctx.getCommandLine();
    String pid = cmd.assertOption(OPT_PROCESS_ID.getName(), true).getValue();
    restartProcessByVmId(ctx, pid);

    while (cmd.hasNext()) {
      sleep(PAUSE);
      if (cmd.isNextArg()) {
        Arg argument = cmd.assertNextArg();
        restartProcessByVmId(ctx, argument.getName());
      } else {
        cmd.next();
      }
    }
  }

  private void restartProcessByVmId(CliContext ctx, String pid) throws InputException {
    Process processToRestart = null;
    Results<List<Process>> results = ctx.getCorus().getProcessorFacade().getProcesses(ProcessCriteria.builder().all(), new ClusterInfo(false));
    while (results.hasNext() && processToRestart == null) {
      Result<List<Process>> result = results.next();
      for (Process process : result.getData()) {
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

  private void restartProcess(CliContext ctx, Process aProcess) throws InputException {
    try {
      KillPreferences prefs = KillPreferences.newInstance().setHard(isHardKillOption(ctx));
      ctx.getCorus().getProcessorFacade().restart(aProcess.getProcessID(), prefs);
    } catch (Exception e) {
      throw new InputException(e.getMessage());
    }
    ctx.getConsole().println("Proceeding to restart of process " + aProcess.getProcessID() + "...");
  }

}
