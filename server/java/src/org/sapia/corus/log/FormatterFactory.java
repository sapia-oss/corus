package org.sapia.corus.log;

import org.apache.log.format.Formatter;
import org.apache.log.format.PatternFormatter;

public class FormatterFactory {
  
  public static Formatter createDefaultFormatter(){
    return new PatternFormatter("%{time:yyyy.MM.dd'@'HH:mm:ss:SSS} %{priority}[%{category}]: %{message} %{throwable}" +
        System.getProperty("line.separator"));
  }
}
