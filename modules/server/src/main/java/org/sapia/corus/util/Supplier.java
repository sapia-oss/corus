package org.sapia.corus.util;

/**
 * A generic supplier interface.
 *  
 * @author yduchesne
 *
 */
public interface Supplier<T> {
  
  /**
   * @return the object of the given type that this instance supplies.
   */
  public T get();

}
