package org.sapia.corus.client.cli;

import java.util.HashMap;
import java.util.Map;

import org.sapia.console.Command;
import org.sapia.console.CommandNotFoundException;
import org.sapia.console.ReflectCommandFactory;

/**
 * A factory of {@link Command}s. Internally caches command instances based on their name.
 * 
 * @author yduchesne
 *
 */
public class CorusCommandFactory extends ReflectCommandFactory {
  
  private Map<String, Command> cachedCommands = new HashMap<String, Command>();

  public CorusCommandFactory() {
    addPackage("org.sapia.corus.client.cli.command");
  }
  
  @Override
  public Command getCommandFor(String name) throws CommandNotFoundException {
    Command cmd = cachedCommands.get(name);
    if (cmd == null) {
      cmd =  super.getCommandFor(name);
      cachedCommands.put(name, cmd);
    }
    return cmd;
  }
}
