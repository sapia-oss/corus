package org.sapia.corus.client.common;

/**
 * Callback specified for logging.
 */
public interface LogCallback {
  void debug(String msg);
  void info(String msg);
  void error(String msg);
}