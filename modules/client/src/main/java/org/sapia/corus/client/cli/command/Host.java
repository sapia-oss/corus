package org.sapia.corus.client.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;

/**
 * Displays info about the host to which the CLI is currently connected.
 * 
 * @author Yanick Duchesne
 */
public class Host extends CorusCliCommand {

  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    StringBuilder hostInfo = new StringBuilder();
    hostInfo.append(ctx.getCorus().getContext().getServerHost().getFormattedAddress()).append(" at domain '")
        .append(ctx.getCorus().getContext().getDomain()).append("'");

    if (ctx.getCorus().getContext().getServerHost().getRepoRole().isClient()) {
      hostInfo.append("(node is a repo)");
    }
    ctx.getConsole().println(hostInfo.toString());
  }
}
