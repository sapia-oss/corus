package org.sapia.corus.client.common.range;

import java.io.Externalizable;
import java.util.List;

import org.sapia.ubik.util.Func;

/**
 * Models "range" behavior.
 * 
 * @author yduchesne
 *
 */
public interface Range<T extends Comparable<T>> extends Externalizable {
  
  /**
   * @return the minimum value in this range.
   */
  public T getMin();
  
  /**
   * @return the maximum value in this range.
   */
  public T getMax();
  
  /**
   * @return the number of values that this range spans.
   */
  public int length();
  
  /**
   * @param value a value to test for.
   * @return <code>true</code> if this range contains the given value.
   */
  public boolean isWithin(T value);

  /**
   * @param value a value to test for.
   * @return <code>true</code> if this range does NOT contain the given value.
   */
  public boolean isOutside(T value);
  
  /**
   * @return a new {@link List} corresponding to all the items in this range, in order.
   */
  public List<T> asList();
  
  /**
   * Creates a list corresponding to each element in this range, as processed by the 
   * given function.
   * 
   * @param function a {@link Func} instance, to which each item in this range is passed,
   * sequentially, and which must return a value for each such item.
   */
  public <R> List<R> asList(Func<R, T> function);

  
  /**
   * @param function a {@link Func} instance, to which each item in this range is passed,
   * sequentially.
   */
  public void forEach(Func<Void, T> function);
  
}
