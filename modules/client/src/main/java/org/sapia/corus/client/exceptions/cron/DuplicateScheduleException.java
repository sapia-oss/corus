package org.sapia.corus.client.exceptions.cron;

import org.sapia.corus.client.exceptions.CorusException;
import org.sapia.corus.client.exceptions.ExceptionCode;

/**
 * Thrown when a given Cron schedule already exists for a process.
 * 
 * @author yduchesne
 * 
 */
public class DuplicateScheduleException extends CorusException {

  static final long serialVersionUID = 1L;

  public DuplicateScheduleException(String msg) {
    super(msg, ExceptionCode.DUPLICATE_SCHEDULE.getFullCode());
  }

}
