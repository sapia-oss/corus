package org.sapia.corus.client.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;

public class Echo extends CorusCliCommand {
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException,
      InputException {
    while (ctx.getCommandLine().hasNext()) {
      ctx.getConsole().println(ctx.getCommandLine().next().toString());      
    }
  }

}