package org.sapia.corus.client.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;


/**
 * @author Yanick Duchesne
 */
public class Resume extends CorusCliCommand {

  @Override
  protected void doExecute(CliContext ctx)
                    throws AbortException, InputException {
    displayProgress(ctx.getCorus().getProcessorFacade().restart(getClusterInfo(ctx)),
                    ctx.getConsole());
  }
}
