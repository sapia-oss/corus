package org.sapia.corus.client.common;

import java.io.Serializable;

/**
 * Models an argument, which can match a given {@link String}.
 * 
 * @author yduchesne
 * 
 */
public interface Arg extends Serializable {

  /**
   * @param str
   *          a {@link String}
   * @return <code>true</code> if this instance matches the given string.
   */
  public boolean matches(String str);

}
