package org.sapia.corus.admin.cli;

import org.sapia.console.Context;
import org.sapia.corus.admin.CorusFacade;


/**
 * @author Yanick Duchesne
 */
public class CliContextImpl extends Context implements CliContext {
  private CorusFacade _corus;

  public CliContextImpl(CorusFacade corus) {
    _corus = corus;
  }

  public CorusFacade getCorus() {
    return _corus;
  }
  
}
