package org.sapia.corus.client.cli.command;

import java.util.List;

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
  protected List<OptionDef> getAvailableOptions() {
    return AVAIL_OPTIONS;
  }
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {

    CmdLine cmd = ctx.getCommandLine();

    // restart ALL
    if (cmd.hasNext() && cmd.isNextArg()) {
      cmd.assertNextArg(new String[] { ARG_ALL });
      new RestartAllCommand().execute((Context) ctx);
    }

    // restart by PROCESS IDENTIFIER
    else if (cmd.containsOption(OPT_PROCESS_ID.getName(), true)) {
      new RestartByVmIdCommand().execute((Context) ctx);

      // restart by OS PROCESS ID
    } else if (cmd.containsOption(OPT_OS_PID.getName(), true)) {
      new RestartByOsPidCommand().execute((Context) ctx);

      // restart by PROCESS DESCRIPTORS
    } else {
      new RestartByProcessDescriptorsCommand().execute((Context) ctx);
    }
  }

}
