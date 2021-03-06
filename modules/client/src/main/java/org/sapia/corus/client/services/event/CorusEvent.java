package org.sapia.corus.client.services.event;

import java.util.Date;

import org.sapia.corus.client.common.json.JsonStream;
import org.sapia.corus.client.services.cluster.CorusHost;

/**
 * Interface common to all events generated by Corus.
 * 
 * @author yduchesne
 *
 */
public interface CorusEvent extends EventLogCapable {

  /**
   * @return the {@link Date} corresponding to the time at which this instance
   * was created.
   */
  public Date getTime();
  
  /**
   * @return the name of the component that is the source of this event.
   */
  public String getSource();
  
  /**
   * @return the type of event.
   */
  public String getType();
  
  /**
   * @return this instance's level.
   */
  public EventLevel getLevel();
   
  /**
   * @param host   the current {@link CorusHost}.
   * @param stream a {@link JsonStream}.
s   */
  public abstract void toJson(CorusHost host, JsonStream stream);
}
