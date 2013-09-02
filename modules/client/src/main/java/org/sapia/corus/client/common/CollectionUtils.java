package org.sapia.corus.client.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides collection-related utilities.
 * 
 * @author yduchesne
 *
 */ 
public class CollectionUtils {

  private CollectionUtils() {
  }
  
  /**
   * @param toReturn the {@link Set} to return, if it is not <code>null</code>.
   * @return the given {@link Set}, or an empty {@link Set} if the given one is empty.
   */
  public static <T> Set<T> emptyIfNull(Set<T> toReturn) {
    if (toReturn == null) {
      return new HashSet<T>();
    }
    return toReturn;
  }
  
  /**
   * @param toReturn the {@link List} to return, if it is not <code>null</code>.
   * @return the given {@link List}, or an empty {@link List} if the given one is empty.
   */
  public static <T> List<T> emptyIfNull(List<T> toReturn) {
    if (toReturn == null) {
      return new ArrayList<T>();
    }
    return toReturn;
  }
}
