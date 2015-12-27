package org.sapia.corus.client.cli;

import java.util.*;

import org.sapia.console.*;

import org.sapia.corus.client.cli.command.Alias;
import org.sapia.corus.client.cli.command.Ant;
import org.sapia.corus.client.cli.command.Appkey;
import org.sapia.corus.client.cli.command.Archive;
import org.sapia.corus.client.cli.command.Cd;
import org.sapia.corus.client.cli.command.Cluster;
import org.sapia.corus.client.cli.command.Cmd;
import org.sapia.corus.client.cli.command.Conf;
import org.sapia.corus.client.cli.command.Connect;
import org.sapia.corus.client.cli.command.Count;
import org.sapia.corus.client.cli.command.Cron;
import org.sapia.corus.client.cli.command.Deploy;
import org.sapia.corus.client.cli.command.Diags;
import org.sapia.corus.client.cli.command.Dir;
import org.sapia.corus.client.cli.command.Docker;
import org.sapia.corus.client.cli.command.Dump;
import org.sapia.corus.client.cli.command.Echo;
import org.sapia.corus.client.cli.command.Env;
import org.sapia.corus.client.cli.command.Err;
import org.sapia.corus.client.cli.command.Exec;
import org.sapia.corus.client.cli.command.Exit;
import org.sapia.corus.client.cli.command.Foreach;
import org.sapia.corus.client.cli.command.Host;
import org.sapia.corus.client.cli.command.Hosts;
import org.sapia.corus.client.cli.command.Http;
import org.sapia.corus.client.cli.command.Kill;
import org.sapia.corus.client.cli.command.Ls;
import org.sapia.corus.client.cli.command.Man;
import org.sapia.corus.client.cli.command.Match;
import org.sapia.corus.client.cli.command.Pause;
import org.sapia.corus.client.cli.command.Port;
import org.sapia.corus.client.cli.command.Ps;
import org.sapia.corus.client.cli.command.Pull;
import org.sapia.corus.client.cli.command.Pwd;
import org.sapia.corus.client.cli.command.Quit;
import org.sapia.corus.client.cli.command.Restart;
import org.sapia.corus.client.cli.command.Resume;
import org.sapia.corus.client.cli.command.Ripple;
import org.sapia.corus.client.cli.command.Role;
import org.sapia.corus.client.cli.command.Rollback;
import org.sapia.corus.client.cli.command.Script;
import org.sapia.corus.client.cli.command.Sort;
import org.sapia.corus.client.cli.command.Status;
import org.sapia.corus.client.cli.command.Suspend;
import org.sapia.corus.client.cli.command.Unarchive;
import org.sapia.corus.client.cli.command.Undeploy;
import org.sapia.corus.client.cli.command.Ver;

/**
 * Holds a static dictionary of CLI commands.
 * 
 * @author yduchesne
 *
 */
public class CommandDict {

  private static final Map<String, Class<? extends Command>> COMMANDS = new HashMap<String, Class<? extends Command>>();

  static {
    COMMANDS.put("alias", Alias.class);
    COMMANDS.put("ant", Ant.class);
    COMMANDS.put("appkey", Appkey.class);
    COMMANDS.put("archive", Archive.class);
    COMMANDS.put("cd", Cd.class);
    COMMANDS.put("cluster", Cluster.class);
    COMMANDS.put("cmd", Cmd.class);
    COMMANDS.put("conf", Conf.class);
    COMMANDS.put("connect", Connect.class);
    COMMANDS.put("count", Count.class);
    COMMANDS.put("cron", Cron.class);
    COMMANDS.put("deploy", Deploy.class);
    COMMANDS.put("diags", Diags.class);
    COMMANDS.put("dir", Dir.class);
    COMMANDS.put("docker", Docker.class);
    COMMANDS.put("dump", Dump.class);
    COMMANDS.put("echo", Echo.class);
    COMMANDS.put("err", Err.class);
    COMMANDS.put("exec", Exec.class);
    COMMANDS.put("exit", Exit.class);
    COMMANDS.put("env", Env.class);
    COMMANDS.put("foreach", Foreach.class);
    COMMANDS.put("host", Host.class);
    COMMANDS.put("hosts", Hosts.class);
    COMMANDS.put("http", Http.class);
    COMMANDS.put("kill", Kill.class);
    COMMANDS.put("ls", Ls.class);
    COMMANDS.put("man", Man.class);
    COMMANDS.put("match", Match.class);
    COMMANDS.put("pause", Pause.class);
    COMMANDS.put("port", Port.class);
    COMMANDS.put("ps", Ps.class);
    COMMANDS.put("pull", Pull.class);
    COMMANDS.put("pwd", Pwd.class);
    COMMANDS.put("quit", Quit.class);
    COMMANDS.put("restart", Restart.class);
    COMMANDS.put("resume", Resume.class);
    COMMANDS.put("ripple", Ripple.class);
    COMMANDS.put("rollback", Rollback.class);
    COMMANDS.put("role", Role.class);
    COMMANDS.put("script", Script.class);
    COMMANDS.put("sort", Sort.class);
    COMMANDS.put("status", Status.class);
    COMMANDS.put("suspend", Suspend.class);
    COMMANDS.put("unarchive", Unarchive.class);
    COMMANDS.put("undeploy", Undeploy.class);
    COMMANDS.put("ver", Ver.class);
  }
  
  /**
   * @param name a command name.
   * @return <code>true</code> if this instance has a command configured for the given name.
   */
  public static boolean hasCommandFor(String name) {
    return COMMANDS.containsKey(name);
  }

  /**
   * @param name a command name.
   * @return the {@link Command} instance corresponding to the given name.
   * @throws CommandNotFoundException if no command could be found for the given name.
   * @throws IllegalStateException if a problem occurred trying to create a {@link Command} instance.
   */
  public static Command instantiateCommandFor(String name) throws CommandNotFoundException, IllegalStateException {
    Class<? extends Command> commandClass = getCommandClassFor(name);
    try {
      return (Command) commandClass.newInstance();
    } catch (Exception e) {
      throw new IllegalStateException("Could not instantiate command for: " + name, e);
    }
  }
  
  /**
   * @return the {@link List} of command names corresponding to the commands that this instance is configured with.
   */
  public static List<String> getCommandNames() {
    return new ArrayList<String>(COMMANDS.keySet());
  }
  
  /**
   * @param name a command name.
   * @return the {@link Command} class corresponding ot the given name.
   * @throws CommandNotFoundException if no such command class is found.
   */
  public static Class<? extends Command> getCommandClassFor(String name) throws CommandNotFoundException {
    Class<? extends Command> clazz = COMMANDS.get(name);
    if (clazz == null) {
      throw new CommandNotFoundException(name);
    }
    return clazz;
  }
  
}
