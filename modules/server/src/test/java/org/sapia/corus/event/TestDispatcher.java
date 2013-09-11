package org.sapia.corus.event;

import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.ubik.rmi.interceptor.Event;
import org.sapia.ubik.rmi.interceptor.Interceptor;
import org.sapia.ubik.rmi.interceptor.InvalidInterceptorException;

public class TestDispatcher implements EventDispatcher {
  
  public void addInterceptor(Class<?> event, Interceptor it)
      throws InvalidInterceptorException {
  }
  
  public void dispatch(Event event) {
  }
  
  public String getRoleName() {
    return EventDispatcher.ROLE;
  }
}
