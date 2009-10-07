package org.sapia.corus.event;

import org.sapia.corus.admin.Module;
import org.sapia.ubik.rmi.interceptor.Event;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.rmi.interceptor.InvalidInterceptorException;

/**
 * Specifies event dispatching/registration behavior.
 * 
 * @author Yanick Duchesne
 */
public interface EventDispatcher extends java.rmi.Remote, Module{
	
  public static String ROLE = EventDispatcher.class.getName();
  
  /**
   * Adds an interceptor for the given event type.
   *
   * @param event an event class.
   * @param it an <code>Interceptor</code> instance.
   *
   * @throws InvalidInterceptorException if the interceptor could not be added.
   */
  public void addInterceptor(Class event, Interceptor it)
  throws InvalidInterceptorException;
  /**
   * Dispatches the given event to all interceptors that have
   * registered for the event's class.
   *
   * @param an <code>Event</code> instance.
   */
  public void dispatch(Event event);

}
