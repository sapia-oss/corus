package org.sapia.corus.client.common.log;

/**
 * A {@link LogCallback} implementation whose instances add a prefix to log messages, 
 * and dispatches these to an arbitrary {@link LogCallback} delegate.
 * 
 * @author yduchesne
 *
 */
public class PrefixedLogCallback implements LogCallback {
  
  private String prefix;
  private LogCallback delegate;
  
  /**
   * @param prefix the prefix to use.
   * @param delegate the delegate {@link LogCallback} to dispatch the log messages to.
   */
  public PrefixedLogCallback(String prefix, LogCallback delegate) {
    this.prefix = prefix;
    this.delegate = delegate;
  }
  
  @Override
  public void debug(String msg) {
    delegate.debug(concat(msg));
  }
  
  @Override
  public void error(String msg) {
    delegate.error(concat(msg));
  }
  
  @Override
  public void info(String msg) {
    delegate.info(concat(msg));
  }
  
  private String concat(String msg) {
    return new StringBuilder(prefix.length() + (msg == null ? 0 : msg.length()) + 1)
      .append(prefix).append(' ')
      .append(msg).toString();
  }
}