package org.sapia.corus.client.cli;

import org.sapia.console.CmdLine;
import org.sapia.console.Console;
import org.sapia.console.Context;
import org.sapia.corus.client.facade.CorusConnector;

/**
 * Implements a context that is used when a command line is spawned from another one.
 * @author yduchesne
 *
 */
public class ChildCliContext extends Context implements CliContext{
  
  private CliContext parent;
  private CmdLine childCmd;
  private Exception error;
  private boolean abortOnError;
  
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
  
  public CorusConnector getCorus() {
    return parent.getCorus();
  }

  @Override
  public Exception getError() {
    return error;
  }
  
  @Override
  public void setError(Exception err) {
    error = err;
  }
 
  @Override
  public boolean isAbordOnError() {
    return abortOnError;
  }
  
  @Override
  public void setAbortOnError(boolean abortOnError) {
    this.abortOnError = abortOnError;
  }
}
