package org.sapia.corus.client.services.event;

import java.util.Date;

import org.sapia.corus.client.common.ThreadSafeDateFormatter;
import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.common.json.JsonStreamable.ContentLevel;
import org.sapia.corus.client.services.cluster.CorusHost;

/**
 * Abstract class to be inherited from by {@link CorusEvent} implementations. Inherithing classes
 * have to implement the {@link EventLogCapable} and {@link rg.sapia.corus.client.common.json.JsonStreamable} interface.
 * 
 * @author yduchesne
 *
 */
public abstract class CorusEventSupport implements CorusEvent {
  
  private static final ThreadSafeDateFormatter FMT = ThreadSafeDateFormatter.getIsoUtcInstance();

  private Date time = new Date();
  
  @Override
  public Date getTime() {
    return time;
  }
  
  @Override
  public String getSource() {
    return source().getSimpleName();
  }

  @Override
  public String getType() {
    return type().getSimpleName();
  }

  @Override
  public void toJson(CorusHost host, JsonStream stream) {
    stream.beginObject()
      .field("source").value(source().getSimpleName())
      .field("type").value(type().getSimpleName())
      .field("level").value(getLevel().name())
      .field("time").value(FMT.format(getTime()))
      .field("host");
    host.toJson(stream, ContentLevel.MINIMAL);
    toJson(stream);
    stream.endObject();
  }
  
  /**
   * Template method to be overloaded by inheriting classes.
   * 
   * @return the class of the object from which the event originates.
   */
  protected abstract Class<?> source();
  
  /**
   * Returns the {@link Class} of the event type, which by default corresponds to this instance's class.
   * 
   * @return this instance's class, which is used as the event type.
   */
  protected Class<?> type() {
    return getClass();
  }
  
  /**
   * Template method to be overloaded by inheriting classes.
   * 
   * @param stream the {@link JsonStream} to write to.
   */
  protected abstract void toJson(JsonStream stream);
  
}
