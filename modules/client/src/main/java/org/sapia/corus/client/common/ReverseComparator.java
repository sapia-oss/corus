package org.sapia.corus.client.common;

import java.util.Comparator;

/**
 * Wraps a given {@link Comparator}, reversing the comparison logic.
 * 
 * @author yduchesne
 *
 * @param <T> the generic type corresponding to the value that this comparator compares.
 */
public class ReverseComparator<T> implements Comparator<T> {
  
  private Comparator<T> delegate;
  
  /**
   * @param delegate the {@link Comparator} to "reverse".
   */
  public ReverseComparator(Comparator<T> delegate) {
    this.delegate = delegate;
  }
  
  @Override
  public int compare(T o1, T o2) {
    return - delegate.compare(o1, o2);
  }

}
