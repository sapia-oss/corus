package org.sapia.corus.client.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.common.CliUtils;
import org.sapia.corus.client.exceptions.cli.SystemExitException;
import org.sapia.ubik.net.TCPAddress;

/**
 * Either terminates the CLI, or connects to the "previous" Corus node in the
 * history - if any.
 * 
 * @author Yanick Duchesne
 */
public class Exit extends CorusCliCommand {

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    if (ctx.getCorus().getContext().getConnectionHistory().isEmpty()) {
      throw new SystemExitException();
    } else {
      TCPAddress previousAddress = (TCPAddress) ctx.getCorus().getContext().getConnectionHistory().pop();
      ctx.getCorus().getContext().reconnect(previousAddress.getHost(), previousAddress.getPort());
      // Change the prompt
      ctx.getConsole().setPrompt(CliUtils.getPromptFor(ctx.getCorus().getContext()));
    }
  }
}
