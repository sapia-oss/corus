package org.sapia.corus.client.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;

/**
 * Echos to the console the value of its argument.
 * 
 * @author yduchesne
 *
 */
public class Echo extends NoOptionCommand {

  @Override
  protected void doInit(CliContext context) {
  }
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    while (ctx.getCommandLine().hasNext()) {
      ctx.getConsole().println(ctx.getCommandLine().next().toString());
    }
  }

}