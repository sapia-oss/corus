package org.sapia.corus.client.services.event;

import java.util.Date;

import org.sapia.corus.client.common.ThreadSafeDateFormatter;
import org.sapia.corus.client.common.json.JsonStreamable;
import org.sapia.ubik.rmi.interceptor.Event;

/**
 * Abstract class to be inherited from by {@link Event} implementations. Inherithing classes
 * have to implement the {@link EventLogCapable} and {@link JsonStreamable} interface.
 * 
 * @author yduchesne
 *
 */
public abstract class EventSupport implements CorusEvent {
  
  private static final ThreadSafeDateFormatter FMT = ThreadSafeDateFormatter.getIsoUtcInstance();

  private Date time = new Date();
  
  @Override
  public Date getTime() {
    return time;
  }
  
  protected String formattedTime() {
    return FMT.format(time);
  }
}
