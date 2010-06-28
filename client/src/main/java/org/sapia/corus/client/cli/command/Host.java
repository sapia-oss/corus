package org.sapia.corus.client.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;


/**
 * @author Yanick Duchesne
 */
public class Host extends CorusCliCommand {

  protected void doExecute(CliContext ctx)
                    throws AbortException, InputException {
    ctx.getConsole().println(ctx.getCorus().getContext().getAddress() +
                             " at domain '" + ctx.getCorus().getContext().getDomain() +
                             "'");
  }
}
