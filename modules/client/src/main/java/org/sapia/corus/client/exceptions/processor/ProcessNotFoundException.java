package org.sapia.corus.client.exceptions.processor;

import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.exceptions.ExceptionCode;

/**
 * Thrown when a given process could not be found.
 * 
 * @author yduchesne
 * 
 */
public class ProcessNotFoundException extends CorusException {

  static final long serialVersionUID = 1L;

  public ProcessNotFoundException(String msg) {
    super(msg, ExceptionCode.PROCESS_NOT_FOUND.getFullCode());
  }

}
