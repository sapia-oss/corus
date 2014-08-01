package org.sapia.corus.client.cli.command;

import java.io.IOException;

import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;

/**
 * Changes the CLI's current directory.
 * 
 * @author yduchesne
 * 
 */
public class Cd extends NoOptionCommand {

  @Override
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    Arg arg = ctx.getCommandLine().assertNextArg();
    try {
      ctx.getFileSystem().changeBaseDir(arg.getName());
    } catch (IOException e) {
      ctx.createAndAddErrorFor(this, e.getMessage(), e);
      ctx.getConsole().println(e.getMessage());
    }
  }

}
