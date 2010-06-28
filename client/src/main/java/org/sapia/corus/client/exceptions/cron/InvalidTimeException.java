package org.sapia.corus.client.exceptions.cron;

import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.exceptions.ExceptionCode;


/**
 * Thrown when a Cron schedule is specified with an invalid time.
 * 
 * @author Yanick Duchesne
 */
public class InvalidTimeException extends CorusException {
  
  static final long serialVersionUID = 1L;
  
  public InvalidTimeException(String msg) {
    super(msg, ExceptionCode.INVALID_TIME.getFullCode());
  }

}
