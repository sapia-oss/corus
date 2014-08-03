package org.sapia.corus.client.cli.command;

import java.util.HashMap;
import java.util.Map;

import org.sapia.console.AbortException;
import org.sapia.console.Arg;
import org.sapia.console.CmdLine;
import org.sapia.console.InputException;
import org.sapia.corus.client.cli.CliContext;
import org.sapia.corus.client.cli.CliError;
import org.sapia.corus.client.cli.help.Help;
import org.sapia.corus.client.cli.help.NoHelpException;

/**
 * Displays command-line doc.
 * 
 * @author Yanick Duchesne
 */
public class Man extends NoOptionCommand {

  private static final Map<String, Class<?>> COMMANDS = new HashMap<String, Class<?>>();

  static {
    COMMANDS.put("ant", Ant.class);
    COMMANDS.put("cd", Cd.class);
    COMMANDS.put("cluster", Cluster.class);
    COMMANDS.put("cmd", Cmd.class);
    COMMANDS.put("conf", Conf.class);
    COMMANDS.put("connect", Connect.class);
    COMMANDS.put("count", Count.class);
    COMMANDS.put("cron", Cron.class);
    COMMANDS.put("deploy", Deploy.class);
    COMMANDS.put("dir", Dir.class);
    COMMANDS.put("err", Err.class);
    COMMANDS.put("exec", Exec.class);
    COMMANDS.put("exit", Exit.class);
    COMMANDS.put("host", Host.class);
    COMMANDS.put("hosts", Hosts.class);
    COMMANDS.put("http", Http.class);
    COMMANDS.put("kill", Kill.class);
    COMMANDS.put("ls", Ls.class);
    COMMANDS.put("man", Man.class);
    COMMANDS.put("pause", Pause.class);
    COMMANDS.put("port", Port.class);
    COMMANDS.put("ps", Ps.class);
    COMMANDS.put("pull", Pull.class);
    COMMANDS.put("pwd", Pwd.class);
    COMMANDS.put("quit", Quit.class);
    COMMANDS.put("restart", Restart.class);
    COMMANDS.put("resume", Resume.class);
    COMMANDS.put("ripple", Ripple.class);
    COMMANDS.put("script", Script.class);
    COMMANDS.put("sort", Sort.class);
    COMMANDS.put("status", Status.class);
    COMMANDS.put("suspend", Suspend.class);
    COMMANDS.put("undeploy", Undeploy.class);
    COMMANDS.put("ver", Ver.class);
  }
  
  @Override
  protected void doInit(CliContext context) {
  }

  protected void doExecute(CliContext ctx) throws AbortException, InputException {
    CmdLine cmd = ctx.getCommandLine();
    if (cmd.hasNext() && cmd.isNextArg()) {
      Arg toDisplayHelp = cmd.assertNextArg();
      Class<?> cmdClass = (Class<?>) COMMANDS.get(toDisplayHelp.getName());
      if (cmdClass == null) {
        ctx.getConsole().out().println("No help available for: " + toDisplayHelp.getName());
        ctx.getConsole().out().println();
        displayHelp(ctx, Man.class);
      } else {
        displayHelp(ctx, cmdClass);
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
