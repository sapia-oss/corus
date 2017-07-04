package org.sapia.corus.event;

import org.sapia.ubik.concurrent.BlockingRef;

public class TestInterceptor {
  
  BlockingRef<TestEvent> eventRef = new BlockingRef<TestEvent>();
  
  public void onTestEvent(TestEvent event) {
    eventRef.set(event);
  }

}
