package org.sapia.corus.client.common;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.ubik.util.Strings;

/**
 * Implements a tuple with 2 values.
 * 
 * @author yduchesne
 */
public class PairTuple<A, B> implements Externalizable {
  
  private A left;
  private B right;
 
  /**
   * DO NOT USE: meant for externalization only.
   */
  public PairTuple() {
  }
  
  public PairTuple(A left, B right) {
    this.left  = left;
    this.right = right;
  }
  
  /**
   * @return this instance's "left" entry.
   */
  public A getLeft() {
    return left;
  }
  
  /**
   * @return this instance's "right" entry.
   */
  public B getRight() {
    return right;
  }
  
  // --------------------------------------------------------------------------
  // Externalizable interface
  
  @SuppressWarnings("unchecked")
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    left = (A) in.readObject();
    right = (B) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(left);
    out.writeObject(right);
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public String toString() {
    return Strings.toStringFor(this, "left", left, "right", right);
  }
  
  @SuppressWarnings("rawtypes")
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PairTuple) {
      PairTuple t = (PairTuple) obj;
      return ObjectUtils.safeEquals(left, t.left) 
          && ObjectUtils.safeEquals(right, t.right);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return ObjectUtils.safeHashCode(left, right);
  }

}
