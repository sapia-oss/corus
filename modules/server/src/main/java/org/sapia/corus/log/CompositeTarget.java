package org.sapia.corus.log;

import java.util.ArrayList;
import java.util.List;

import org.apache.log.LogEvent;
import org.apache.log.LogTarget;

/**
 * A composite {@link LogTarget} that encapsulates other log targets to which it
 * dispatches incoming {@link LogEvent}s.
 * 
 * @author yduchesne
 * 
 */
public class CompositeTarget implements LogTarget {

  private List<LogTarget> targets = new ArrayList<LogTarget>();

  public void processEvent(LogEvent evt) {
    for (LogTarget t : targets) {
      t.processEvent(evt);
    }
  }

  /**
   * @param nested
   *          a {@link LogTarget} add to this instance.
   */
  public void addTarget(LogTarget nested) {
    targets.add(nested);
  }

}
