package org.sapia.corus.client;

import org.sapia.ubik.net.ServerAddress;
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

  private ServerAddress origin;
  private T data;

  public Result(ServerAddress origin, T data) {
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
   * @return the {@link ServerAddress} corresponding to the address of the Corus
   *         server on which the invocation was performed.
   */
  public ServerAddress getOrigin() {
    return origin;
  }

  @Override
  public String toString() {
    return Strings.toStringFor(this, "origin", origin, "data", data);
  }
}
