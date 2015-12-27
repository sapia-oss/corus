package org.sapia.corus.client.cli.command;

import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.console.CommandNotFoundException;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CliError;
import org.sapia.corus.client.cli.CommandDict;
import org.sapia.corus.client.cli.help.Help;
import org.sapia.corus.client.cli.help.NoHelpException;

/**
 * Displays command-line doc.
 * 
 * @author Yanick Duchesne
 */
public class Man extends NoOptionCommand {


  
  @Override
  protected void doInit(CliContext context) {
  }

  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    CmdLine cmd = ctx.getCommandLine();
    if (cmd.hasNext() && cmd.isNextArg()) {
      Arg toDisplayHelp = cmd.assertNextArg();
      if (!CommandDict.hasCommandFor(toDisplayHelp.getName())) {
        ctx.getConsole().out().println("No help available for: " + toDisplayHelp.getName());
        ctx.getConsole().out().println();
        displayHelp(ctx, Man.class);
      } else {
        try {
          displayHelp(ctx, CommandDict.getCommandClassFor(toDisplayHelp.getName()));
        } catch (CommandNotFoundException e) {
          throw new IllegalStateException("Unexpected error occurred while trying to display command help for: " + toDisplayHelp.getName(), e);
        }
      } 
    } else {
      displayHelp(ctx, Man.class);
    }
  }

  private void displayHelp(CliContext ctx, Class<?> clazz) {
    Help h;
    try {
      h = Help.newHelpFor(clazz);
      h.display(ctx.getConsole().out());

    } catch (NoHelpException e) {
      ctx.getConsole().out().println("No help available.");

    } catch (Exception e) {
      CliError err = ctx.createAndAddErrorFor(this, "Could not display help", e);
      ctx.getConsole().println(err.getSimpleMessage());
    }
  }

}
