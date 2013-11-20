package org.sapia.corus.client.exceptions.processor;

import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.exceptions.ExceptionCode;

/**
 * Thrown when a given process configuration could not be found.
 * 
 * @author yduchesne
 * 
 */
public class ProcessConfigurationNotFoundException extends CorusException {

  static final long serialVersionUID = 1L;

  public ProcessConfigurationNotFoundException(String msg) {
    super(msg, ExceptionCode.PROCESS_CONFIG_NOT_FOUND.getFullCode());
  }

}
