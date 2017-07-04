package org.sapia.corus.client.services.event;

import org.sapia.corus.client.Module;
import org.sapia.ubik.rmi.interceptor.InvalidInterceptorException;

/**
 * Specifies event dispatching/registration behavior.
 * 
 * @author yduchesne
 */
public interface EventDispatcher extends Module {

  public static String ROLE = EventDispatcher.class.getName();

  /**
   * Adds an interceptor for the given event type.
   * 
   * @param event
   *          an event class.
   * @param it
   *          an interceptor object.
   * 
   * @throws InvalidInterceptorException
   *           if the interceptor could not be added.
   */
  public void addInterceptor(Class<?> event, Object it) throws InvalidInterceptorException;

  /**
   * Dispatches the given event to all interceptors that have registered for the
   * event's class.
   * 
   * @param event
   *          an {@link Object} acting as an event.
   */
  public void dispatch(Object event);

}
