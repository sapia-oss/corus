package org.sapia.corus.client.common.json;

/**
 * Implemented by classes whose objects can serialize themselves to a
 * {@link JsonStream}.
 * 
 * @author yduchesne
 *
 */
public interface JsonStreamable {

  /**
   * @param stream a {@link JsonStream}.
   */
  public void toJson(JsonStream stream);
}
