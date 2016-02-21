package org.sapia.corus.client.common.log;

/**
 * Callback specified for logging.
 */
public interface LogCallback {
  
  void debug(String msg);
  void info(String msg);
  void error(String msg);
  
  class NullLogCallback implements LogCallback {

    @Override
    public void debug(String msg) {
    }

    @Override
    public void info(String msg) {
    }

    @Override
    public void error(String msg) {
    }
    
  }
  
}