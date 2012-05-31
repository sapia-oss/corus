package org.sapia.corus.client.cli;

import java.util.ArrayList;
import java.util.List;

import org.sapia.console.AbortException;
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
  private boolean 			 abortOnError;

  /**
   * Creates a new {@link CliContextImpl} instance.
   *
   * @param corus
   */
  public CliContextImpl(CorusConnector corus, List<CliError> anErrorList) {
    this.corus = corus;
    errors = anErrorList;
  }
  
  public CorusConnector getCorus() {
    return corus;
  }

  public CliError createAndAddErrorFor(CorusCliCommand aCommand, Throwable aCause) {
    CliError created = null;
    synchronized (errors) {
      created = new CliError(ERROR_COUNTER++, null, aCause, getCommandLine(), aCommand);
      errors.add(created);
    }
    
    if (abortOnError) {
      throw new AbortException();
    }
    
    return created;
  }

  public CliError createAndAddErrorFor(CorusCliCommand aCommand, String aDescription, Throwable aCause) {
    CliError created = null;
    synchronized (errors) {
      created = new CliError(ERROR_COUNTER++, aDescription, aCause, getCommandLine(), aCommand);
      errors.add(created);
    }

    if (abortOnError) {
      throw new AbortException();
    }    
    
    return created;
  }

  public List<CliError> getErrors() {
    synchronized (errors) {
      return new ArrayList<CliError>(errors);
    }
  }

  public int removeAllErrors() {
    int size = 0;
    synchronized (errors) {
      size = errors.size();
      errors.clear();
    }
    
    return size;
  }

  public boolean isAbordOnError() {
    return abortOnError;
  }

  public void setAbortOnError(boolean abortOnError) {
    this.abortOnError = abortOnError;
  }
  
}
