package org.sapia.corus.admin.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.corus.admin.cli.CliContext;


/**
 * @author Yanick Duchesne
 */
public class Host extends CorusCliCommand {

  protected void doExecute(CliContext ctx)
                    throws AbortException, InputException {
    ctx.getConsole().println(ctx.getCorus().getServerAddress() +
                             " at domain '" + ctx.getCorus().getDomain() +
                             "'");
  }
}
