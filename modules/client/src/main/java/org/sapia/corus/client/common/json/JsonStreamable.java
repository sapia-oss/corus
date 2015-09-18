package org.sapia.corus.client.common.json;

import org.sapia.corus.client.common.ToStringUtils;
import org.sapia.ubik.util.Strings;

/**
 * Implemented by classes whose objects can serialize themselves to a
 * {@link JsonStream}.
 * 
 * @author yduchesne
 *
 */
public interface JsonStreamable {
  
  /**
   * Holds content level constants.
   * 
   * @author yduchesne
   *
   */
  public enum ContentLevel {
   
    MINIMAL(0),
    SUMMARY(1),
    DETAIL(2); 
    
    private int value;
    
    private ContentLevel(int value) {
      this.value = value;
    }
    
    public boolean greaterThan(ContentLevel other) {
      return value > other.value;
    }
    
    public boolean greaterOrEqualThan(ContentLevel other) {
      return value >= other.value;
    }
    
    /**
     * This method returns the {@link ContentLevel} corresponding to the given name - it is case-insensitive.
     * 
     * @param name a content level name.
     * @return {@link ContentLevel} matching the given name.
     * @throws IllegalArgumentException if the name given corresponds to an invalid content level name.
     */
    public static ContentLevel forName(String name) throws IllegalArgumentException {
      for (ContentLevel l : ContentLevel.values()) {
        if (name.equalsIgnoreCase(l.name())) {
          return l;
        }
      }
      throw new IllegalArgumentException("Invalid content level. Expected one of: " + 
          ToStringUtils.joinToString((Object[])ContentLevel.values()));
    }
    
    /**
     * This method returns the {@link ContentLevel} correspdonding to the given name, or the given default 
     * level if the name provided is <code>null</code> or an empty string.
     * 
     * @param name a content level name.
     * @param defaultLevel a default {@link ContentLevel}.
     * @return the {@link ContentLevel} matching the given name if it applies, or the given {@link ContentLevel}.
     * @throws IllegalArgumentException if the name given corresponds to an invalid content level name.
     */
    public static ContentLevel forNameOrDefault(String name, ContentLevel defaultLevel) throws IllegalArgumentException {
      if (Strings.isBlank(name)) {
        return defaultLevel;
      } else {
        return forName(name);
      }
    }
  }
  
  // ==========================================================================

  /**
   * @param stream a {@link JsonStream}.
   * @param level the current {@link ContentLevel}.
   */
  public void toJson(JsonStream stream, ContentLevel level);
}
