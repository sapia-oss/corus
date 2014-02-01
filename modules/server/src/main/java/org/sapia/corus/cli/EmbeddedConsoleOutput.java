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
  private Logger logger = Hierarchy.getDefaultHierarchy().getLoggerFor("EmbeddedCli");
  
  @Override
  public synchronized void flush() {
    if (buffer.length() > 0) {
      logger.info(buffer.toString());
      buffer.delete(0,  buffer.length());
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
    flush();
    logger.info("");
  }
  
  @Override
  public synchronized void println(String s) {
    flush();
    logger.info(s);
  }
  
}
