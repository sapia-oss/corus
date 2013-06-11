package org.sapia.corus.client.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;

/**
 * Displays the path to the CLI's current directory.
 * 
 * @author yduchesne
 *
 */
public class Pwd extends CorusCliCommand {
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException,
      InputException {
    ctx.getConsole().println(ctx.getFileSystem().getBaseDir().getAbsolutePath());
  }

}
