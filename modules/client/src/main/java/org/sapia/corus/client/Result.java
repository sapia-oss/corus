package org.sapia.corus.client;

import org.sapia.corus.client.services.cluster.CorusHost;
import org.sapia.ubik.util.Strings;

/**
 * An instance of this class holds the return value of a clustered method
 * invocation.
 * 
 * @author yduchesne
 * 
 * @param <T>
 */
public class Result<T> {

  private CorusHost origin;
  private T data;

  public Result(CorusHost origin, T data) {
    this.origin = origin;
    this.data = data;
  }

  /**
   * @return the return data of the remote invocation, or <code>null</code> if
   *         the invocation returned nothing.
   */
  public T getData() {
    return data;
  }

  /**
   * @return the {@link CorusHost} corresponding to the address of the Corus
   *         server on which the invocation was performed.
   */
  public CorusHost getOrigin() {
    return origin;
  }

  @Override
  public String toString() {
    return Strings.toStringFor(this, "origin", origin, "data", data);
  }
}
