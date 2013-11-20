package org.sapia.corus.client.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.text.StrLookup;
import org.sapia.console.AbortException;
import org.sapia.console.CmdLine;
import org.sapia.console.Console;
import org.sapia.console.Context;
import org.sapia.corus.client.cli.command.CorusCliCommand;
import org.sapia.corus.client.facade.CorusConnector;

/**
 * @author Yanick Duchesne
 */
public class CliContextImpl extends Context implements CliContext {

  private static int ERROR_COUNTER = 1;

  private CorusConnector corus;
  private List<CliError> errors;
  private boolean abortOnError;
  private StrLookup vars;

  /**
   * Creates a new {@link CliContextImpl} instance.
   * 
   * @param corus
   */
  public CliContextImpl(CorusConnector corus, List<CliError> anErrorList, StrLookup vars) {
    this.corus = corus;
    this.errors = anErrorList;
    this.vars = vars;
  }

  @Override
  public CorusConnector getCorus() {
    return corus;
  }

  @Override
  public ClientFileSystem getFileSystem() {
    return corus.getContext().getFileSystem();
  }

  @Override
  public StrLookup getVars() {
    return vars;
  }

  @Override
  public CliError createAndAddErrorFor(CorusCliCommand aCommand, Throwable aCause) {
    CliError created = null;
    synchronized (errors) {
      created = new CliError(ERROR_COUNTER++, null, aCause, getCommandLine(), aCommand);
      errors.add(created);
    }

    if (aCause instanceof AbortException) {
      throw (AbortException) aCause;
    } else if (abortOnError) {
      throw new AbortException("Error occurred", aCause);
    }

    return created;
  }

  @Override
  public CliError createAndAddErrorFor(CorusCliCommand aCommand, String aDescription, Throwable aCause) {
    CliError created = null;
    synchronized (errors) {
      created = new CliError(ERROR_COUNTER++, aDescription, aCause, getCommandLine(), aCommand);
      errors.add(created);
    }

    if (aCause instanceof AbortException) {
      throw (AbortException) aCause;
    } else if (abortOnError) {
      throw new AbortException("Error occurred", aCause);
    }

    return created;
  }

  @Override
  public List<CliError> getErrors() {
    synchronized (errors) {
      return new ArrayList<CliError>(errors);
    }
  }

  @Override
  public int removeAllErrors() {
    int size = 0;
    synchronized (errors) {
      size = errors.size();
      errors.clear();
    }

    return size;
  }

  @Override
  public boolean isAbordOnError() {
    return abortOnError;
  }

  @Override
  public void setAbortOnError(boolean abortOnError) {
    this.abortOnError = abortOnError;
  }

  @Override
  protected void setUp(Console cons, CmdLine cmdLine) {
    super.setUp(cons, cmdLine);
  }

}
