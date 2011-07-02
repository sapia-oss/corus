package org.sapia.corus.client.exceptions.processor;

import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.exceptions.ExceptionCode;

public class TooManyProcessInstanceException  extends CorusException{
  
  static final long serialVersionUID = 1L;
  
  public TooManyProcessInstanceException(String msg) {
    super(msg, ExceptionCode.TOO_MANY_PROCESS_INSTANCES.getFullCode());
  }  

}
