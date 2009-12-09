package org.sapia.corus.admin.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.corus.admin.cli.CliContext;


/**
 * @author Yanick Duchesne
 */
public class Quit extends CorusCliCommand {

  @Override
  protected void doExecute(CliContext ctx)
                    throws AbortException, InputException {
    throw new AbortException();
  }
}
