package org.sapia.corus.client.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;

/**
 * Forces a repo pull.
 * 
 * @author yduchesne
 */
public class Pull extends CorusCliCommand {

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    ctx.getCorus().getRepoFacade().pull(getClusterInfo(ctx));
  }

}
