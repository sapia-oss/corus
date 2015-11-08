package org.sapia.corus.client.annotations;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Holds constants corresponding to different priority levels.
 * 
 * @see Priority
 * 
 * @author yduchesne
 *
 */
public enum PriorityLevel {

  MINIMAL(0),
  LOW(1),
  HIGH(2),
  MAJOR(3),
  EXTREME(4);
  
  private int value;
  
  private PriorityLevel(int value) {
    this.value = value;
  }
  
  /**
   * @return the actual value corresponding to the priority level.
   */
  public int value() {
    return value;
  }
  
  /**
   * @return a {@link Comparator} for sorting {@link PriorityLevel}s in descending order
   * (from highest to lowest priority).
   */
  public static Comparator<PriorityLevel> comparator() {
    return new Comparator<PriorityLevel>() {
      @Override
      public int compare(PriorityLevel o1, PriorityLevel o2) {
        return -(o1.value - o2.value);
      }
    };
  }
  
  /**
   * Compares the elements in the given list on the basis of their respective
   * {@link PriorityLevel}. Assumes that the class of each object has been annotated
   * with the {@link Priority} annotation - if that is not the case, the {@link PriorityLevel#MINIMAL}
   * priority is assumed.
   * 
   * @param objects a {@link List} of {@link Object}s to compare.
   */
  public static void sort(List<?> objects) {
    Collections.sort(objects, new Comparator<Object>() {
      Comparator<PriorityLevel> cmp = PriorityLevel.comparator();
      @Override
      public int compare(Object o1, Object o2) {
        PriorityLevel p1 = getPriorityLevelFor(o1); 
        PriorityLevel p2 = getPriorityLevelFor(o2);
        return cmp.compare(p1, p2);
      }
    });
  }
  
  private static PriorityLevel getPriorityLevelFor(Object o) {
    if (o.getClass().isAnnotationPresent(Priority.class)) {
      Priority p = o.getClass().getAnnotation(Priority.class);
      return p.value();
    }
    return PriorityLevel.MINIMAL;
  }
}
