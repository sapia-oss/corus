package org.sapia.corus.client.exceptions.processor;

import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.exceptions.ExceptionCode;

/**
 * Thrown when attempting to execute instances of a process, but the maximum number
 * of instances has already been reached.
 * 
 * @author yduchesne
 *
 */
public class TooManyProcessInstanceException  extends CorusException{
  
  static final long serialVersionUID = 1L;
  
  public TooManyProcessInstanceException(String msg) {
    super(msg, ExceptionCode.TOO_MANY_PROCESS_INSTANCES.getFullCode());
  }  

}
