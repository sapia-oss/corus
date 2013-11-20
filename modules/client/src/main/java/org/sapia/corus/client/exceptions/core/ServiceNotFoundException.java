package org.sapia.corus.client.exceptions.core;

import org.sapia.corus.client.exceptions.CorusRuntimeException;
import org.sapia.corus.client.exceptions.ExceptionCode;

/**
 * Thrown when a given looked up service is not found.
 * 
 * @author yduchesne
 * 
 */
public class ServiceNotFoundException extends CorusRuntimeException {

  static final long serialVersionUID = 1L;

  public ServiceNotFoundException(String msg) {
    super(msg, ExceptionCode.SERVICE_NOT_FOUND.getFullCode());
  }

}
