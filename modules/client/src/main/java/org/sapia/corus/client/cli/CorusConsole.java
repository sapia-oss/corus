package org.sapia.corus.client.cli;

import org.sapia.console.CommandFactory;

/**
 * Interface specifying methods common to all types of Corus client consoles 
 * that use a {@link CorusCommandFactory}.
 * 
 * @author yduchesne
 *
 */
public interface CorusConsole {
  
  /**
   * @return this instance's {@link CommandFactory}.ß
   */
  public CorusCommandFactory getCommands();

}
