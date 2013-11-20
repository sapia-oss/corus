package org.sapia.corus.client.cli.command.exec;

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
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessCriteria;

/**
 * Implements the logic to restart process by OS pid (
 * <code>restart -op 12345 12346</code>).
 * 
 * @author yduchesne
 * 
 */
public class RestartByOsPidCommand extends AbstractExecCommand {

  private static final long PAUSE = 1000;

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {

    CmdLine cmd = ctx.getCommandLine();
    String osPid = cmd.assertOption(OS_PID_OPT, true).getValue();
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
    }
  }

  private void restartProcess(CliContext ctx, Process aProcess) throws InputException {
    try {
      ctx.getCorus().getProcessorFacade().restart(aProcess.getProcessID());
    } catch (Exception e) {
      throw new InputException(e.getMessage());
    }
    ctx.getConsole().println("Proceeding to restart of process " + aProcess.getProcessID() + "...");
  }

}
