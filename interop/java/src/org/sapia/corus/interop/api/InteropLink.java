/*
 * IopLink.java
 *
 * Created on October 26, 2005, 9:54 AM
 */

package org.sapia.corus.interop.api;

/**
 *
 * @author yduchesne
 */
public class InteropLink {
  
  private static Implementation _impl;
  
  /** Creates a new instance of IopLink */
  public InteropLink() {
  }
  
  /**
   * Sets this instance's implementation, if it hasn't been already set.
   *
   * @param impl an <code>Implementation</code>.
   */
  public static void setImpl(Implementation impl){
    if(_impl == null)
      _impl = impl;
  }
  
  /**
   * @return this instance's <code>Implementation</code>.
   */
  public static Implementation getImpl(){
    return _impl == null ? new NullImplementation() : _impl;
  }
  
}
