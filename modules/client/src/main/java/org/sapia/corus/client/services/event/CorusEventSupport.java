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
  public void toJson(CorusHost host, JsonStream stream) {
    stream.beginObject()
      .field("source").value(source().getSimpleName())
      .field("type").value(getClass().getSimpleName())
      .field("level").value(getLevel().name())
      .field("time").value(FMT.format(getTime()))
      .field("host");
    host.toJson(stream, ContentLevel.MINIMAL);
    toJson(stream);
    stream.endObject();
  }
  
  /**
   * @return this instance's formatted time - as a UTC-based timestamp.
   */
  protected String formattedTime() {
    return FMT.format(time);
  }
  
  /**
   * Template method to be overloaded by inheriting classes.
   * 
   * @return the class of the object from which the event originates.
   */
  protected abstract Class<?> source();
  
  /**
   * Template method to be overloaded by inheriting classes.
   * 
   * @param stream the {@link JsonStream} to write to.
   */
  protected abstract void toJson(JsonStream stream);
  
}
