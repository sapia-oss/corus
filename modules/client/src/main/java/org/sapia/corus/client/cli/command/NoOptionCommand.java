package org.sapia.corus.client.cli.command;

import java.util.ArrayList;

import org.sapia.console.CmdLine;
import org.sapia.console.InputException;

/**
 * To be inherited by commands that have no options, or want to bypass option validation.
 * @author yduchesne
 *
 */
public abstract class NoOptionCommand extends CorusCliCommand {

  
  @Override
  protected void validate(CmdLine cmdLine) throws InputException {
  }
  
  protected java.util.List<OptionDef> getAvailableOptions() {
    return new ArrayList<CorusCliCommand.OptionDef>();
  }
}
