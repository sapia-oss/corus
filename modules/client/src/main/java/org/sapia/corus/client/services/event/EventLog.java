package org.sapia.corus.client.services.event;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;

import org.sapia.corus.client.common.ThreadSafeDateFormatter;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.ubik.util.Assertions;

/**
 * Holds key event data in a generic representation.
 * 
 * @author yduchesne
 *
 */
public class EventLog implements JsonStreamable, Externalizable {

  private static final ThreadSafeDateFormatter FMT = ThreadSafeDateFormatter.getIsoUtcInstance();
  
  private Date        time;
  private String      source;
  private String      type;
  private String      message;
  private EventLevel  level;

  /**
   * Do not call: meant for externalization only.
   */
  public EventLog() {
    
  }
  
  public static Builder builder() {
    return new Builder();
  }
  
  public EventLog(Date time, EventLevel level, String source, String type, String message) {
    this.time    = time;
    this.level   = level;
    this.source  = source;
    this.type    = type;
    this.message = message;
  }
  
  public EventLog(Date time, EventLevel level, String source, Class<?> type, String message) {
    this(time, level, source, type.getSimpleName(), message);
  }
  
  public Date getTime() {
    return time;
  }

  public EventLevel getLevel() {
    return level;
  }
  
  public String getType() {
    return type;
  }
  
  public String getSource() {
    return source;
  }

  public String getMessage() {
    return message;
  }
  
  // --------------------------------------------------------------------------
  // JsonStreamable interface
  
  @Override
  public void toJson(JsonStream stream, ContentLevel level) {
    stream.beginObject()
      .field("time").value(FMT.format(time))
      .field("level").value(level.name())
      .field("source").value(source)
      .field("type").value(type)
      .field("message").value(message)
      .endObject();
  }

  // --------------------------------------------------------------------------
  // Externalizable interface
  
  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    time = (Date) in.readObject();
    level = (EventLevel) in.readObject();
    source = in.readUTF();
    type = in.readUTF();
    message = in.readUTF();
  }
  
  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(time);
    out.writeObject(level);
    out.writeUTF(source);
    out.writeUTF(type);
    out.writeUTF(message);
  }
  
  // --------------------------------------------------------------------------
  // Object overrides

  public String toString() {
    return level + "," + source + "," + message;
  }
  
  public static class Builder {
    
    private Date   time;
    private String source;
    private String type;
    private String message;
    private EventLevel  level;
    
    public Builder time(Date time) {
      this.time = time;
      return this;
    } 
    
    public Builder source(String source) {
      this.source = source;
      return this;
    }
    
    public Builder type(String type) {
      this.type = type;
      return this;
    }
    
    public Builder type(Class<?> type) {
      this.type = type.getSimpleName();
      return this;
    }
    
    public Builder message(String message, String...args) {
      this.message = String.format(message, args);
      return this;
    }
    
    public Builder level(EventLevel level) {
      this.level = level;
      return this;
    }
    
    public EventLog build() {
      Assertions.notNull(source, "Source must be specified");
      Assertions.notNull(type, "Type must be specified");
      Assertions.notNull(message, "Message must be specified");
      Assertions.notNull(level, "Level must be specified");
      
      return new EventLog(time == null ? new Date() : time, level, source, type, message);
    }
    
  }

}
