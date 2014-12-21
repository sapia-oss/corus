package org.sapia.corus.client.cli;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.Command;
import org.sapia.console.CommandNotFoundException;
import org.sapia.console.Context;
import org.sapia.console.InputException;
import org.sapia.console.ReflectCommandFactory;
import org.sapia.corus.client.cli.command.CorusCliCommand;

/**
 * A factory of {@link Command}s. Internally caches command instances based on their name.
 * 
 * @author yduchesne
 *
 */
public class CorusCommandFactory extends ReflectCommandFactory {
  
  private Map<String, Command>      cachedCommands = new HashMap<>();
  private Map<String, AliasCommand> aliases        = new HashMap<>();

  public CorusCommandFactory() {
    addPackage("org.sapia.corus.client.cli.command");
  }
  
  /**
   * Returns a {@link Map} from alias names to aliased commands.
   * 
   * @return the {@link Map} of {@link AliasCommand}s that this instance holds.
   */
  public Map<String, AliasCommand> getAliases() {
    return Collections.unmodifiableMap(aliases);
  }
  
  /**
   * @param aliasName the name of the alias to create.
   * @param commandName the name of the command to alias.
   * @param cmdLine the command's arguments.
   * @throws CommandNotFoundException if no command corresponding to the passed in command name
   * could be found.
   */
  public void addAlias(String aliasName, String commandName, CmdLine cmdLine) throws CommandNotFoundException {
    Command delegate = cachedCommands.get(commandName);
    if (delegate == null) {
      delegate = super.getCommandFor(commandName);
      cachedCommands.put(commandName, delegate);
    }
    aliases.put(aliasName, new AliasCommand(delegate, commandName, cmdLine));
  }
  
  /**
   * Removes the given alias.
   * 
   * @param aliasName the name of an existing alias.
   */
  public void removeAlias(String aliasName) {
    aliases.remove(aliasName);
  }
  
  @Override
  public Command getCommandFor(String name) throws CommandNotFoundException {
    if (aliases.containsKey(name)) {
      return aliases.get(name);
    } else {
      Command cmd = cachedCommands.get(name);
      if (cmd == null) {
        cmd =  super.getCommandFor(name);
        cachedCommands.put(name, cmd);
      }
      return cmd;
    }
  }
  
  /**
   * Method for adding commands dynamically, for test purposes.
   * 
   * @param name the name of the {@link Command} being added.
   * @param command a {@link Command}, which in fact should be an instance of {@link CorusCliCommand}.
   */
  public void addCommand(String name, Command command) {
    cachedCommands.put(name, command);
  }
  
  // --------------------------------------------------------------------------
  
  /**
   * A {@link CorusCliCommand} that wraps an aliased command.
   * 
   * @author yduchesne
   *
   */
  public static class AliasCommand extends CorusCliCommand {
    
    private Command delegate;
    private String  commandName;
    private CmdLine cmdLine;
    
    private AliasCommand(Command delegate, String commandName, CmdLine cmdLine) {
      this.delegate    = delegate;
      this.commandName = commandName;
      this.cmdLine     = cmdLine;
    }
    
    /**
     * @return the {@link CmdLine} instance holding the options/arguments to pass to the
     * aliased command.
     */
    public CmdLine getCmdLine() {
      return cmdLine;
    }
    
    /**
     * @return the name of the aliased command.ß∂
     */
    public String getAliasedCommand() {
      return commandName;
    }
    
    @Override
    public void execute(Context ctx) throws AbortException, InputException {
      CliContext      parent = (CliContext) ctx;
      
      // if the alias has no arguments/options, using command-line passed in
      if (!cmdLine.hasNext()) {
        delegate.execute(ctx);
     
      // the passed in command has no arguments/options, using alias arguments/options
      } else if (!ctx.getCommandLine().hasNext()) {
        ChildCliContext child  = new ChildCliContext(parent, cmdLine, parent.getVars());
        delegate.execute(child);
        
      // if both alias and original command have arguments/options, overriding with alias  
      } else {
        ChildCliContext child  = new ChildCliContext(parent, cmdLine, parent.getVars());
        delegate.execute(child);
      }
    }
    
    @Override
    protected void doExecute(CliContext ctx) throws AbortException,
        InputException {
      throw new IllegalStateException("doExecute() should not be called");
    }
    
    @Override
    public List<OptionDef> getAvailableOptions() {
      throw new IllegalStateException("getAvailableOptions() should not be called");
    }
    
    @Override
    protected void doInit(CliContext context) {
    }
    
  }
}
