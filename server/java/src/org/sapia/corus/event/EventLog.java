package org.sapia.corus.event;

public class EventLog {
  
  public enum Level{
    NORMAL,
    WARNING,
    CRITICAL,
    FATAL,
  }
  
  private String source, message;
  private Level level;
  
  public EventLog(Level level, String source, String message) {
    this.level = level;
    this.source = source;
    this.message = message;
  }
  
  public Level getLevel() {
    return level;
  }
  
  public String getMessage() {
    return message;
  }
  
  public String getSource() {
    return source;
  }
  
  public String toString(){
    return level + "," + source + "," + message;
  }
  

}
