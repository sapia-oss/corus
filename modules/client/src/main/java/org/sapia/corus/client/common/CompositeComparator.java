package org.sapia.corus.client.common;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A composite {@link Comparator} implementation, taking potentially multiple other
 * {@link Comparator}s and delegating to them comparison logic.
 * <p>
 * The delegate comparators are invoked in the order in which they were added to this
 * instance.
 * 
 * @author yduchesne
 */
public class CompositeComparator<T> implements Comparator<T> {

  private List<Comparator<T>> delegates = new ArrayList<Comparator<T>>();
  
  /**
   * Adds the given {@link Comparator} to this instance.
   * 
   * @param delegate a delegate {@link Comparator}.
   * @return this instance.
   */
  public CompositeComparator<T> add(Comparator<T> delegate) {
    delegates.add(delegate);
    return this;
  }
  
  @Override
  public int compare(T arg0, T arg1) {
    int c = 0;
    for (Comparator<T> d : delegates) {
      c = d.compare(arg0, arg1);
      if (c != 0) {
        break;
      }
    }
    return c;
  }
}
