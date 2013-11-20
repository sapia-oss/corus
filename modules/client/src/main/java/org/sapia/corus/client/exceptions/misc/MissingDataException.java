package org.sapia.corus.client.exceptions.misc;

import org.sapia.corus.client.exceptions.CorusRuntimeException;
import org.sapia.corus.client.exceptions.ExceptionCode;

/**
 * Thrown when expected data is missing.
 * 
 * @author yduchesne
 * 
 */
public class MissingDataException extends CorusRuntimeException {

  static final long serialVersionUID = 1L;

  public MissingDataException(String msg) {
    super(msg, ExceptionCode.MISSING_DATA.getFullCode());
  }

}
