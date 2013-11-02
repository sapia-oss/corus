package org.sapia.corus.client.cli;

import java.util.List;

import org.apache.commons.lang.text.StrLookup;
import org.sapia.console.CmdLine;
import org.sapia.console.Console;
import org.sapia.console.Context;
import org.sapia.corus.client.cli.command.CorusCliCommand;
import org.sapia.corus.client.facade.CorusConnector;

/**
 * Implements a context that is used when a command line is spawned from another one.
 * @author yduchesne
 *
 */
public class ChildCliContext extends Context implements CliContext {
  
  private CliContext parent;
  private CmdLine 	 childCmd;
  private boolean 	 abortOnError;
  private StrLookup  vars;
  
  public ChildCliContext(CliContext parent, CmdLine childCmd, StrLookup vars) {
    this.parent   = parent;
    this.childCmd = childCmd;
    this.vars     = vars;
  }
  
  @Override
  public StrLookup getVars() {
    return vars;
  }
  
  @Override
  public CmdLine getCommandLine() {
    return childCmd;
  }
  
  @Override
  public Console getConsole() {
    return parent.getConsole();
  }
  
  @Override
  public ClientFileSystem getFileSystem() {
    return parent.getFileSystem();
  }

  @Override
  public CorusConnector getCorus() {
    return parent.getCorus();
  }

  @Override
  public CliError createAndAddErrorFor(CorusCliCommand aCommand, Throwable aCause) {
    return parent.createAndAddErrorFor(aCommand, aCause);
  }
  
  @Override
  public CliError createAndAddErrorFor(CorusCliCommand aCommand, String aDescription, Throwable aCause) {
    return parent.createAndAddErrorFor(aCommand, aDescription, aCause);
  }
  
  @Override
  public List<CliError> getErrors() {
    return parent.getErrors();
  }

  @Override
  public int removeAllErrors() {
    return parent.removeAllErrors();
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
