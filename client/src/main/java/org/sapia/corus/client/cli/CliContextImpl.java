package org.sapia.corus.client.cli;

import org.sapia.console.Context;
import org.sapia.corus.client.facade.CorusConnector;


/**
 * @author Yanick Duchesne
 */
public class CliContextImpl extends Context implements CliContext {
  private CorusConnector _corus;

  public CliContextImpl(CorusConnector corus) {
    _corus = corus;
  }

  public CorusConnector getCorus() {
    return _corus;
  }
  
}
