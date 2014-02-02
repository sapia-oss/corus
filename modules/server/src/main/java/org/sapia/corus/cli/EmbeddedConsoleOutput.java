package org.sapia.corus.cli;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.sapia.console.ConsoleOutput;

/**
 * Redirectes console output to Corus' log file.
 * 
 * @author yduchesne
 *
 */
public class EmbeddedConsoleOutput implements ConsoleOutput {
  
  private StringBuilder buffer = new StringBuilder();
  private Logger logger;
  
  /**
   * Default ctor.
   */
  public EmbeddedConsoleOutput() {
    this(Hierarchy.getDefaultHierarchy().getLoggerFor("EmbeddedCli"));
  }
  
  /**
   * @param logger the {@link Logger} to log to.
   */
  protected EmbeddedConsoleOutput(Logger logger) {
    this.logger = logger;
  }
  
  @Override
  public synchronized void flush() {
    if (buffer.length() > 0) {
      logger.info(buffer());
    }
  }
  
  @Override
  public synchronized void print(char c) {
    buffer.append(c);
  }

  @Override
  public synchronized void print(String s) {
    buffer.append(s);
  }
  
  @Override
  public synchronized void println() {
    logger.info(buffer() + "");
  }
  
  @Override
  public synchronized void println(String s) {
    logger.info(buffer() + s);
  }
  
  private synchronized String buffer() {
    String toReturn = buffer.toString();
    buffer.delete(0, buffer.length());
    return toReturn;
  }
  
}
