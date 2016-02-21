package org.sapia.corus.client.common.tuple;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.sapia.corus.client.common.ObjectUtil;
import org.sapia.ubik.util.Strings;

/**
 * Implements a tuple with 3 values.
 * 
 * @author yduchesne
 */
public class TripleTuple<A, B, C> implements Externalizable {
  
  private A first;
  private B second;
  private C third;
 
  /**
   * DO NOT USE: meant for externalization only.
   */
  public TripleTuple() {
  }
  
  public TripleTuple(A first, B second, C third) {
    this.first  = first;
    this.second = second;
    this.third  = third;
  }
  
  public A getFirst() {
    return first;
  }
  
  public A get_0() {
    return first;
  }
  
  public B getSecond() {
    return second;
  }
  
  public B get_1() {
    return second;
  }
  
  public C getThird() {
    return third;
  }
  
  public C get_2() {
    return third;
  }
  
  // --------------------------------------------------------------------------
  // Externalizable interface
  
  @SuppressWarnings("unchecked")
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    first  = (A) in.readObject();
    second = (B) in.readObject();
    third  = (C) in.readObject();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(first);
    out.writeObject(second);
    out.writeObject(third);
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @Override
  public String toString() {
    return Strings.toStringFor(this, "first", first, "second", second, "third", third);
  }
  
  @SuppressWarnings("rawtypes")
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof TripleTuple) {
      TripleTuple t = (TripleTuple) obj;
      return ObjectUtil.safeEquals(first,  t.first) 
          && ObjectUtil.safeEquals(second, t.second)
          && ObjectUtil.safeEquals(third, t.third);
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return ObjectUtil.safeHashCode(first, second, third);
  }

}
