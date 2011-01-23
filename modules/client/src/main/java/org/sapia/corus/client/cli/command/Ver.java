package org.sapia.corus.client.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;

public class Ver extends CorusCliCommand{
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException,
      InputException {
    ctx.getConsole().println("Server version: " + ctx.getCorus().getContext().getVersion());
  }

}
