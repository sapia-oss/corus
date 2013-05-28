package org.sapia.corus.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.sapia.ubik.util.Condition;

/**
 * Holds collection-related utilities.
 * 
 * @author yduchesne
 *
 */
public class CollectionUtil {
  
  private CollectionUtil() {
  }
  
  /**
   * @param toConvert a {@link Collection} to convert.
   * @param condition a {@link Condition} to apply.
   * @return a {@link List} containing the items for which the given {@link Condition} returned <code>true</code>.
   */
  public static final <T> List<T> filterToArrayList(Collection<T> toFilter, Condition<T> condition) {
    List<T> result = new ArrayList<T>();
    for (T c : toFilter) {
      if (condition.apply(c)) {
        result.add(c);
      }
    }
    return result;
  } 

}
