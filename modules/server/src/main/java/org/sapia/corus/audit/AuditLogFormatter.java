package org.sapia.corus.audit;

import org.apache.log.format.Formatter;

/**
 * A {@link Formatter} which formats audit log messages.
 * 
 * @author yduchesne
 *
 */
public class AuditLogFormatter implements Formatter {
  
  public String format(org.apache.log.LogEvent event) {
    return event.getMessage();
  }

}
