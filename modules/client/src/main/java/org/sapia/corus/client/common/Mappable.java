package org.sapia.corus.client.common;

import java.util.Map;

/**
 * To be implemented by classes whose instances can export their state to a {@link Map}.
 * 
 * @author yduchesne
 *
 */
public interface Mappable {
  
  /**
   * @return a {@link Map} representation of this instance's fields.
   */
  public Map<String, Object> asMap();

}
