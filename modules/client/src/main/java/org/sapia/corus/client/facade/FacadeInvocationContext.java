package org.sapia.corus.client.facade;

import org.sapia.corus.client.common.ProgressQueue;

public class FacadeInvocationContext {

  private static final ThreadLocal<Object> RETURN_VALUE = new ThreadLocal<Object>();

  private FacadeInvocationContext() {
  }

  /**
   * @param returnValue
   *          the {@link Object} corresponding to the return value of the last
   *          method invocation on a facade.
   */
  public static void set(Object returnValue) {
    RETURN_VALUE.set(returnValue);
  }

  /**
   * Unsets the return value of the last method invocation on a facade.s
   */
  public static void unset() {
    RETURN_VALUE.set(null);
  }

  /**
   * @return the {@link Object} corresponding to the return value of the last
   *         method invocation on a facade.
   */
  public static Object get() {
    Object toReturn = RETURN_VALUE.get();
    if (toReturn instanceof ProgressQueue) {
      ((ProgressQueue) toReturn).waitFor();
    }
    return toReturn;
  }
}
