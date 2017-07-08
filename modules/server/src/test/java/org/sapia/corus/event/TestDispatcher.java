package org.sapia.corus.event;

import org.sapia.corus.client.services.event.EventDispatcher;
import org.sapia.ubik.rmi.interceptor.InvalidInterceptorException;

public class TestDispatcher implements EventDispatcher {
  
  public void addInterceptor(Class<?> event, Object it)
      throws InvalidInterceptorException {
  }
  
  public void dispatch(Object event) {
  }
  
  public String getRoleName() {
    return EventDispatcher.ROLE;
  }
}
