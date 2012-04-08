package org.sapia.corus.client.cli;

import java.util.List;

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
  
  /* (non-Javadoc)
   * @see org.sapia.corus.client.cli.CliContext#getCorus()
   */
  public CorusConnector getCorus() {
    return parent.getCorus();
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.client.cli.CliContext#createAndAddErrorFor(org.sapia.corus.client.cli.command.CorusCliCommand, java.lang.Throwable)
   */
  public CliError createAndAddErrorFor(CorusCliCommand aCommand, Throwable aCause) {
    return parent.createAndAddErrorFor(aCommand, aCause);
  }
  /* (non-Javadoc)
   * @see org.sapia.corus.client.cli.CliContext#createAndAddErrorFor(org.sapia.corus.client.cli.command.CorusCliCommand, java.lang.String, java.lang.Throwable)
   */
  public CliError createAndAddErrorFor(CorusCliCommand aCommand, String aDescription, Throwable aCause) {
    return parent.createAndAddErrorFor(aCommand, aDescription, aCause);
  }
  
  /* (non-Javadoc)
   * @see org.sapia.corus.client.cli.CliContext#getErrors()
   */
  public List<CliError> getErrors() {
    return parent.getErrors();
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.client.cli.CliContext#removeAllErrors()
   */
  public int removeAllErrors() {
    return parent.removeAllErrors();
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.client.cli.CliContext#isAbordOnError()
   */
  public boolean isAbordOnError() {
    return abortOnError;
  }
  
  /* (non-Javadoc)
   * @see org.sapia.corus.client.cli.CliContext#setAbortOnError(boolean)
   */
  public void setAbortOnError(boolean abortOnError) {
    this.abortOnError = abortOnError;
  }
}
