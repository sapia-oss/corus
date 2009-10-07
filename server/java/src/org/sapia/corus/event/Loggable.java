package org.sapia.corus.event;

/**
 * Specifies the behavior for obtaining an {@link EventLog}. Event classes that 
 * implement this interface thus return a "loggable" representation of their instances.
 * 
 * @author yduchesne
 *
 */
public interface Loggable {
  
  /**
   * @return an {@link EventLog}.
   */
  public EventLog getEventLog();
}
