package org.sapia.corus.client.common.range;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.client.common.ObjectUtil;
import org.sapia.ubik.util.Assertions;
import org.sapia.ubik.util.Func;

/**
 * Implements a {@link Range} of {@link Integer}s.
 * 
 * @author yduchesne
 *
 */
public class IntRange implements Range<Integer> { 
  
  private int min, max;

  /**
   * DO NOT CALL: meant for externalization only.
   */
  public IntRange() {
  }
  
  public IntRange(int min, int max) {
    Assertions.isTrue(min <= max, "Min (%s) must be smaller than/equal to max (%s)", min, max);
    this.min = min;
    this.max = max;
  }

  @Override
  public Integer getMin() {
    return min;
  }

  @Override
  public Integer getMax() {
    return max;
  }

  @Override
  public boolean isWithin(Integer value) {
    return value >= min && value <= max;
  }

  @Override
  public boolean isOutside(Integer value) {
    return value < min && value > max;
  }
  
  @Override
  public int length() {
    return max - min + 1;
  }

  @Override
  public List<Integer> asList() {
    List<Integer> values = new ArrayList<>(length());
    for (int i = min; i <= max; i++) {
      values.add(i);
    }
    return values;
  }
  
  @Override
  public <R> List<R> asList(Func<R, Integer> function) {
    List<R> values = new ArrayList<>(length());
    for (int i = min; i <= max; i++) {
      values.add(function.call(i));
    }
    return values;
  }

  @Override
  public void forEach(Func<Void, Integer> function) {
    for (int i = min; i <= max; i++) {
      function.call(i);
    }
  }
  
  public static Range<Integer> forLength(int len) {
    return new IntRange(0, len - 1);
  }
  
  public static Range<Integer> forLength(int min, int len) {
    return new IntRange(min, min + len - 1);
  }
  
  public static Range<Integer> forMax(int max) {
    return new IntRange(0, max);
  }
  
  public static Range<Integer> forBounds(int min, int max) {
    return new IntRange(min, max);
  }
  
  // --------------------------------------------------------------------------
  // Object overrides
  
  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Range) {
      Range<?> other = (Range<?>) obj;
      if (other.getMin().getClass().equals(Integer.class)) {
        Range<Integer> otherIntRange = (Range<Integer>) other;
        return min == otherIntRange.getMin().intValue() 
            && max == otherIntRange.getMax().intValue();
      }
      return false;
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    return ObjectUtil.safeHashCode(min, max);
  }
  
  @Override
  public String toString() {
    return new StringBuilder("[").append(min).append(", ").append(max).append("]").toString();
  }
  
  // --------------------------------------------------------------------------
  // Externalizable interface
  
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    min = in.readInt();
    max = in.readInt();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeInt(min);
    out.writeInt(max);
  }
 
}
