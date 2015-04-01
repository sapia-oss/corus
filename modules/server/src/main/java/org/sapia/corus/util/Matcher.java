package org.sapia.corus.util;

/**
 * This interface specifies matching behavior.
 * 
 * @author yduchesne
 * 
 */
public interface Matcher<T> {

  /**
   * @param object
   * @return true of the given object matches this instance's criteria.
   */
  public boolean matches(T object);

  // --------------------------------------------------------------------------

  public static class MatchAll<T> implements Matcher<T> {
    public boolean matches(T object) {
      return true;
    }

  }

}
