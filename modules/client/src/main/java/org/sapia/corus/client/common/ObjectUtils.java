package org.sapia.corus.client.common;

import java.util.List;

public class ObjectUtils {

  private static final int PRIME = 31;

  private ObjectUtils() {
  }

  /**
   * Returns the given object's hash code, or 0 if the object is
   * <code>null</code>.
   * 
   * @param o
   *          an {@link Object}.
   * @return a hash code for the given object.
   */
  public static int safeHashCode(Object o) {
    return o == null ? 0 : o.hashCode() * PRIME;
  }

  /**
   * Returns a hash code for all the given objects, even if some of them are
   * <code>null</code>.
   * 
   * @param objects
   *          one or many {@link Object}s.
   * @return a hash code resulting from the composition of all the given
   *         objects' individual hash codes.
   */
  public static int safeHashCode(Object... objects) {
    if (objects == null) {
      return 0;
    }
    int hash = 0;
    for (Object o : objects) {
      hash += safeHashCode(o);
    }
    return hash;
  }

  /**
   * Handles the equality test of both parameters, even if one or both of them
   * is/are <code>null</code>.
   * 
   * @param a
   *          an {@link Object}
   * @param b
   *          another {@link Object}.
   * @return <code>true</code> if a and b are deemed equal.
   */
  public static boolean safeEquals(Object a, Object b) {
    if (a == null && b == null) {
      return true;
    } else if (a != null && b != null) {
      return a.equals(b);
    } else {
      return false;
    }
  }
  
  /**
   * Compares two lists for equality.
   * 
   * @param a a {@link List} to compare for equality.
   * @param b another {@link List}, to compare with the previous one.
   * @return <code>true</code> if both lists have the same size and their 
   * respective elements are equal.
   */
  public static boolean safeListEquals(List<?> a, List<?> b) {
    if (a.size() == b.size()) {
      for (int i = 0; i < a.size(); i++) {
        if (!safeEquals(a.get(i), b.get(i))) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
  
  /**
   * @param instances an array of arbitrary objects, matching the given generic type.
   * @return the first non-null instance that is found in the given array.
   * @throws IllegalArgumentException if all no non-null instance is found.
   */
  @SafeVarargs
  public static <T> T safeNonNull(T...instances) throws IllegalArgumentException {
    for (T i : instances) {
      if (i != null) {
        return i;
      }
    }
    throw new IllegalArgumentException("At least one non-null instance expected in passed in array");
  }
}
