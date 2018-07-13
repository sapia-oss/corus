package org.sapia.corus.client.services.event;

import org.sapia.corus.client.common.ToStringUtil;

/**
 * Holds constants corresponding to the different event levels.
 * 
 * @author yduchesne
 *
 */
public enum EventLevel {
  TRACE(0), 
  INFO(1), 
  WARNING(2), 
  ERROR(3), 
  CRITICAL(4), 
  FATAL(5);
  
  private int level;
  
  private EventLevel(int level) {
    this.level = level;
  }
  
  /**
   * @param threshold an {@link EventLevel} that serves as threshold.
   * @return if this instance's level is equal to or greater than the given threshold.
   */
  public boolean isAtLeast(EventLevel threshold) {
    return level >= threshold.level;
  }
  
  /**
   * @param levelName the name an {@link EventLevel}.
   * @return the {@link EventLevel} corresponding to the given name.
   * @throws IllegalArgumentException if no {@link EventLevel} corresponds to the
   *         provided name.
   */
  public static EventLevel forName(String levelName) {
    for (EventLevel v : EventLevel.values()) {
      if (v.name().equalsIgnoreCase(levelName)) {
        return v;
      }
    }
    throw new IllegalArgumentException(String.format(
        "Invalid level %s. Expected one of: %s", 
        levelName, 
        ToStringUtil.joinAsCsv(EventLevel.values()))
    );
  }
}