package org.sapia.corus.client.common.log;

/**
 * Extends the {@link LogCallback} interface with richer behavior.
 * 
 * @author yduchesne
 *
 */
public interface ExtendedLogCallback extends LogCallback {

  /**
   * @return <code>true</code> if the DEBUG level is enabled.
   */
  public boolean isDebugEnabled();
  
  /**
   * @return <code>true</code> if the INFO level is enabled.
   */
  public boolean isInfoEnabled();

  
  class NullExtendedLogCallback implements ExtendedLogCallback {

    @Override
    public void debug(String msg) {
    }

    @Override
    public void info(String msg) {
    }

    @Override
    public void error(String msg) {
    }
    
    @Override
    public boolean isDebugEnabled() {
      return true;
    }
    
    @Override
    public boolean isInfoEnabled() {
      return true;
    }
  }
}
