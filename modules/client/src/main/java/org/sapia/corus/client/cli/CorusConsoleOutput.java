package org.sapia.corus.client.cli;

import org.sapia.console.ConsoleOutput;

/**
 * Specifies a {@link ConsoleOutput} that allows turning output on/off.
 * 
 * @author yduchesne
 *
 */
public interface CorusConsoleOutput extends ConsoleOutput {

  // ==========================================================================
  // Interface methods

  /**
   * Disables output.
   */
  public void turnOff();
  
  /**
   * Enables output.
   */
  public void turnOn();
  
  // ==========================================================================
  // Default impl.
  
  /**
   * Implements the {@link CorusConsoleOutput} interface around a delegate {@link ConsoleOutput}.
   * 
   * @author yduchesne
   *
   */
  public static class DefaultCorusConsoleOutput implements CorusConsoleOutput {
    
    private volatile boolean on = true;
    private ConsoleOutput    delegate;
    
    DefaultCorusConsoleOutput(ConsoleOutput delegate) {
      this.delegate = delegate;
    }
    
    @Override
    public void flush() {
      if (on) {
        delegate.flush();
      }
    }
    
    @Override
    public void print(char c) {
      if (on) {
        delegate.print(c);
      }
    }
    
    @Override
    public void print(String s) {
      if (on) {
        delegate.print(s);
      }
    }
    
    @Override
    public void println() {
      if (on) {
        delegate.println();
      }
    }
    
    @Override
    public void println(String s) {
      if (on) {
        delegate.println(s);
      }
    }
    
    // ------------------------------------------------------------------------
    // CorusConsoleOutput implementation
    
    @Override
    public void turnOff() {
      on = false;
    }
    
    @Override
    public void turnOn() {
      on = true;
    }
    
    // ------------------------------------------------------------------------
    // Static methods

    /**
     * @param delegate a {@link ConsoleOutput} to wrap.
     * @return the {@link CorusConsoleOutput} wrapping the given {@link ConsoleOutput}.
     */
    public static CorusConsoleOutput wrap(ConsoleOutput delegate) {
      if (delegate instanceof CorusConsoleOutput) {
        return (CorusConsoleOutput) delegate;
      }
      return new DefaultCorusConsoleOutput(delegate);
    }

  }
}
