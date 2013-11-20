package org.sapia.corus.client.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.Context;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.command.exec.RestartAllCommand;
import org.sapia.corus.client.cli.command.exec.RestartByOsPidCommand;
import org.sapia.corus.client.cli.command.exec.RestartByProcessDescriptorsCommand;
import org.sapia.corus.client.cli.command.exec.RestartByVmIdCommand;

/**
 * Restarts processes.
 * 
 * @author Yanick Duchesne
 */
public class Restart extends AbstractExecCommand {

  public static final int DEFAULT_RESTART_WAIT_TIME_SECONDS = 180;

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {

    CmdLine cmd = ctx.getCommandLine();

    // restart ALL
    if (cmd.hasNext() && cmd.isNextArg()) {
      cmd.assertNextArg(new String[] { ARG_ALL });
      new RestartAllCommand().execute((Context) ctx);
    }

    // restart by PROCESS IDENTIFIER
    else if (cmd.containsOption(VM_ID_OPT, true)) {
      new RestartByVmIdCommand().execute((Context) ctx);

      // restart by OS PROCESS ID
    } else if (cmd.containsOption(OS_PID_OPT, true)) {
      new RestartByOsPidCommand().execute((Context) ctx);

      // restart by PROCESS DESCRIPTORS
    } else {
      new RestartByProcessDescriptorsCommand().execute((Context) ctx);
    }
  }

}
