package org.sapia.corus.client.cli.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.CmdLine;
import org.sapia.console.CommandNotFoundException;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CorusCommandFactory.AliasCommand;
import org.sapia.corus.client.cli.CorusConsole;
import org.sapia.corus.client.common.ArgMatchers;
import org.sapia.ubik.util.Collects;

/**
 * Allows creating and deleting aliases.
 * 
 * @author yduchesne
 *
 */
public class Alias extends CorusCliCommand {

  public static final String ARG_DEL = "del";
  public static final String ARG_LS  = "ls";

  public static final OptionDef OPT_NAME         = new OptionDef("n", true);
  public static final OptionDef OPT_COMMAND_LINE = new OptionDef("c", true);
  
  @Override
  protected void doInit(CliContext context) {
  }
  
  @Override
  public List<OptionDef> getAvailableOptions() {
    return Collects.arrayToList(OPT_NAME, OPT_COMMAND_LINE);
  }
  
  @Override
  protected void doExecute(CliContext ctx) throws AbortException,
      InputException {
    
    if (ctx.getCommandLine().hasNext() && ctx.getCommandLine().isNextArg()) {
      Arg cmd = ctx.getCommandLine().assertNextArg(new String[] { ARG_DEL, ARG_LS });
      if (cmd.getName().equals(ARG_DEL)) {
        String alias = ctx.getCommandLine()
            .assertOption(OPT_NAME.getName(), true).getValue();
        ((CorusConsole) ctx.getConsole()).getCommands().removeAlias(ArgMatchers.parse(alias));
      } else {
        List<AliasCommand> aliases = new ArrayList<>(((CorusConsole) ctx.getConsole()).getCommands().getAliases().values());
        Collections.sort(aliases);
        for (AliasCommand a : aliases) {
          ctx.getConsole().println(a.getAlias() + " >> "
              + a.getAliasedCommand() + " " + a.getCmdLine());
        }
      }
    } else {
      String alias = ctx.getCommandLine()
          .assertOption(OPT_NAME.getName(), true).getValue();
   
      String cmd   = ctx.getCommandLine()
          .assertOption(OPT_COMMAND_LINE.getName(), true).getValue();
     
      CmdLine cmdLine = CmdLine.parse(cmd);
      if (!cmdLine.hasNext()) {
        throw new InputException("Aliased command-line cannot be empty");
      } else if (cmdLine.hasNext() && cmdLine.isNextArg()) {
        Arg cmdName = cmdLine.chopArg();
        try {
          ((CorusConsole) ctx.getConsole()).getCommands().addAlias(alias, cmdName.getName(), cmdLine);
        } catch (CommandNotFoundException e) {
          throw new InputException("Command not found: " + cmdName.getName());
        }
      } else {
        throw new InputException("Command name must be first input in aliased command-line: " + cmd);
      }
    }
  }

}
