package org.sapia.corus.log;

import org.apache.log.format.Formatter;
import org.apache.log.format.PatternFormatter;

/**
 * A factor of {@link Formatter}s.
 * 
 * @author yduchesne
 *
 */
public class FormatterFactory {
  
	/**
	 * Creates a new {@link Formatter} and returns it.
	 * 
	 * @return a new {@link Formatter} instance.
	 */
  public static Formatter createDefaultFormatter(){
    return new PatternFormatter("%{time:yyyy.MM.dd'@'HH:mm:ss:SSS} %{priority}[%{category}]: %{message} %{throwable}" +
        System.getProperty("line.separator"));
  }
}
