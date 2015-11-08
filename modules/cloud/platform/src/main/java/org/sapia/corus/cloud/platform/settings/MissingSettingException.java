package org.sapia.corus.cloud.platform.settings;

/**
 * Thrown when a desired setting is missing.
 * 
 * @see Settings#getNotNull(String)
 * 
 * @author yduchesne
 *
 */
public class MissingSettingException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public MissingSettingException(String msg) {
    super(msg);
  }
 
}
