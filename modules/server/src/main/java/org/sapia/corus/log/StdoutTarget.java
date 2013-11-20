package org.sapia.corus.log;

import org.apache.log.LogEvent;
import org.apache.log.LogTarget;
import org.apache.log.format.Formatter;

/**
 * Implements the {@link LogTarget} over standard output.
 * 
 * @author yduchesne
 * 
 */
public class StdoutTarget implements LogTarget {

  private Formatter formatter;

  public StdoutTarget(Formatter formatter) {
    this.formatter = formatter;
  }

  public void processEvent(LogEvent evt) {
    System.out.print(formatter.format(evt));
  }

}
