package org.sapia.corus.event;

import org.sapia.ubik.concurrent.BlockingRef;
import org.sapia.ubik.rmi.interceptor.Interceptor;

public class TestInterceptor implements Interceptor {
  
  BlockingRef<TestEvent> eventRef = new BlockingRef<TestEvent>();
  
  public void onTestEvent(TestEvent event) {
    eventRef.set(event);
  }

}
