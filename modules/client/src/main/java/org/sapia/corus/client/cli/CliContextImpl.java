package org.sapia.corus.client.cli;

import java.util.ArrayList;
import java.util.List;

import org.sapia.console.Context;
import org.sapia.corus.client.cli.command.CorusCliCommand;
import org.sapia.corus.client.facade.CorusConnector;


/**
 * @author Yanick Duchesne
 */
public class CliContextImpl extends Context implements CliContext {
  
  private static int _ERROR_COUNTER = 1;
  
  private CorusConnector _corus;
  private List<CliError> _errors; 
  private boolean abortOnError;

  /**
   * Creates a new {@link CliContextImpl} instance.
   *
   * @param corus
   */
  public CliContextImpl(CorusConnector corus, List<CliError> anErrorList) {
    _corus = corus;
    _errors = anErrorList;
  }
  
  /* (non-Javadoc)
   * @see org.sapia.corus.client.cli.CliContext#getCorus()
   */
  public CorusConnector getCorus() {
    return _corus;
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.client.cli.CliContext#createAndAddErrorFor(org.sapia.corus.client.cli.command.CorusCliCommand, java.lang.Throwable)
   */
  public CliError createAndAddErrorFor(CorusCliCommand aCommand, Throwable aCause) {
    CliError created = null;
    synchronized (_errors) {
      created = new CliError(_ERROR_COUNTER++, null, aCause, getCommandLine(), aCommand);
      _errors.add(created);
    }
    
    return created;
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.client.cli.CliContext#createAndAddErrorFor(org.sapia.corus.client.cli.command.CorusCliCommand, java.lang.String, java.lang.Throwable)
   */
  public CliError createAndAddErrorFor(CorusCliCommand aCommand, String aDescription, Throwable aCause) {
    CliError created = null;
    synchronized (_errors) {
      created = new CliError(_ERROR_COUNTER++, aDescription, aCause, getCommandLine(), aCommand);
      _errors.add(created);
    }
    
    return created;
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.client.cli.CliContext#getErrors()
   */
  public List<CliError> getErrors() {
    synchronized (_errors) {
      return new ArrayList<CliError>(_errors);
    }
  }

  /* (non-Javadoc)
   * @see org.sapia.corus.client.cli.CliContext#removeAllErrors()
   */
  public int removeAllErrors() {
    int size = 0;
    synchronized (_errors) {
      size = _errors.size();
      _errors.clear();
    }
    
    return size;
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
