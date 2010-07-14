package org.sapia.corus.client.cli;

import org.sapia.console.Context;
import org.sapia.corus.client.facade.CorusConnector;


/**
 * @author Yanick Duchesne
 */
public class CliContextImpl extends Context implements CliContext {
  private CorusConnector _corus;
  private Exception error;
  private boolean abortOnError;

  public CliContextImpl(CorusConnector corus) {
    _corus = corus;
  }

  public CorusConnector getCorus() {
    return _corus;
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
