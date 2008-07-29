package org.sapia.corus.event;

import org.sapia.corus.Module;
import org.sapia.ubik.rmi.interceptor.Event;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.rmi.interceptor.InvalidInterceptorException;

/**
 * Specifies event dispatching/registration behavior.
 * 
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2004 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
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
