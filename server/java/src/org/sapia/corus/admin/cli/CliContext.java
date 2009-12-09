package org.sapia.corus.admin.cli;

import org.sapia.console.CmdLine;
import org.sapia.console.Console;
import org.sapia.corus.admin.CorusFacade;

public interface CliContext {

  public CorusFacade getCorus();

  public CmdLine getCommandLine();
  
  public Console getConsole();
  
}