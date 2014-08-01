package org.sapia.corus.client.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.exceptions.cli.SystemExitException;

/**
 * Terminates the CLI.
 * 
 * @author Yanick Duchesne
 */
public class Quit extends NoOptionCommand {

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    throw new SystemExitException();
  }
}
