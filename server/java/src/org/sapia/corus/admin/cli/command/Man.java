package org.sapia.corus.admin.cli.command;

import java.util.HashMap;
import java.util.Map;

import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.corus.admin.cli.CliContext;
import org.sapia.corus.admin.cli.help.Help;
import org.sapia.corus.admin.cli.help.NoHelpException;
import org.sapia.corus.exceptions.CorusException;

/**
 * @author Yanick Duchesne
 */
public class Man extends CorusCliCommand{
  
  private static Map _commands = new HashMap();
  
  static{
    _commands.put("conf", Conf.class);
    _commands.put("cron", Cron.class);
    _commands.put("deploy", Deploy.class);
    _commands.put("exec", Exec.class);
    _commands.put("exit", Exit.class);
    _commands.put("host", Host.class);
    _commands.put("hosts", Hosts.class);
    _commands.put("kill", Kill.class);
    _commands.put("ls", Ls.class);
    _commands.put("ps", Ps.class);
    _commands.put("restart", Restart.class);
    _commands.put("resume", Resume.class);
    _commands.put("suspend", Suspend.class);
    _commands.put("undeploy", Undeploy.class);
    _commands.put("man", Man.class);
    _commands.put("status", Status.class);
    _commands.put("port", Port.class);
    _commands.put("ver", Ver.class);
  }

  
  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    CmdLine cmd = ctx.getCommandLine();
    if(cmd.hasNext() && cmd.isNextArg()){
      Arg toDisplayHelp = cmd.assertNextArg();
      Class cmdClass = (Class)_commands.get(toDisplayHelp.getName());
      if(cmdClass == null){
        ctx.getConsole().out().println("No help available for: " + toDisplayHelp.getName());
        ctx.getConsole().out().println();
        displayHelp(ctx, Man.class);
      } else{
        displayHelp(ctx, cmdClass);
      }
    } else{
      displayHelp(ctx, Man.class);
    }
  }
  private void displayHelp(CliContext ctx, Class clazz){
    Help h;
    try{
      h = Help.newHelpFor(clazz);
      h.display(ctx.getConsole().out());
    }catch(CorusException e){
      ctx.getConsole().out().println("Could not display help");
      e.printStackTrace(ctx.getConsole().out());
    }catch(NoHelpException e){
      ctx.getConsole().out().println("No help available.");
    }
    
  }
}
