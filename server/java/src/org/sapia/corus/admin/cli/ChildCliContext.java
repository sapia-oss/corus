package org.sapia.corus.admin.cli;

import org.sapia.console.CmdLine;
import org.sapia.console.Console;
import org.sapia.console.Context;
import org.sapia.corus.admin.CorusFacade;

public class ChildCliContext extends Context implements CliContext{
  
  private CliContext parent;
  private CmdLine childCmd;
  
  public ChildCliContext(CliContext parent, CmdLine childCmd) {
    this.parent = parent;
    this.childCmd = childCmd;
  }
  
  @Override
  public CmdLine getCommandLine() {
    return childCmd;
  }
  
  @Override
  public Console getConsole() {
    return parent.getConsole();
  }
  
  public CorusFacade getCorus() {
    return parent.getCorus();
  }

}
