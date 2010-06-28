package org.sapia.corus.client.cli;

import org.sapia.console.CmdLine;
import org.sapia.console.Console;
import org.sapia.corus.client.facade.CorusConnector;

/**
 * This interface specifies the behavior common to
 * command line contexts.
 * 
 * @author yduchesne
 *
 */
public interface CliContext {

  /**
   * @return the {@link CorusConnector} used by the
   * command line interface.
   */
  public CorusConnector getCorus();

  /**
   * @return the {@link CmdLine} instance corresponding to the command
   * line that was entered.
   */
  public CmdLine getCommandLine();
  
  /**
   * @return the {@link Console} that is used to interact with the user.
   */
  public Console getConsole();
  
}