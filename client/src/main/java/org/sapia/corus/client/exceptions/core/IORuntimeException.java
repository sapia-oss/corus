package org.sapia.corus.client.exceptions.core;

import org.sapia.corus.client.exceptions.CorusRuntimeException;
import org.sapia.corus.client.exceptions.ExceptionCode;

/**
 * @author Yanick Duchesne
 */
public class IORuntimeException extends CorusRuntimeException {

  static final long serialVersionUID = 1L;

  public IORuntimeException(Throwable err) {
    super("IO problem occurred performing operation", ExceptionCode.IO_ERROR.getFullCode(), err);
  }

  public IORuntimeException(String msg) {
    super(msg, ExceptionCode.IO_ERROR.getFullCode());
  }

  public IORuntimeException(String msg, Throwable err) {
    super(msg, ExceptionCode.IO_ERROR.getFullCode(),err);
  }
}
