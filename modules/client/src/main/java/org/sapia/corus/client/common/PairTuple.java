package org.sapia.corus.client.common;

import org.sapia.ubik.util.Strings;

/**
 * Implements a tuple with 2 values.
 * 
 * @author yduchesne
 */
public class PairTuple<A, B> {
  
  private A left;
  private B right;
  
  public PairTuple(A left, B right) {
    this.left  = left;
    this.right = right;
  }
  
  public A getLeft() {
    return left;
  }
  
  public B getRight() {
    return right;
  }
  
  @Override
  public String toString() {
    return Strings.toStringFor(this, "left", left, "right", right);
  }

}
