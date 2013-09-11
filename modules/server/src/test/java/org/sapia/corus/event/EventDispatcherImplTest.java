package org.sapia.corus.event;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class EventDispatcherImplTest {

  private EventDispatcherImpl dispatcher;
  
  @Before
  public void setUp() throws Exception {
    dispatcher = new EventDispatcherImpl();
    dispatcher.init();
  }
  
  @After
  public void tearDown() {
    dispatcher.dispose();
  }
  
  @Test
  public void testDispatch() throws Exception {
    TestInterceptor interceptor = new TestInterceptor();
    dispatcher.addInterceptor(TestEvent.class, interceptor);
    dispatcher.dispatch(new TestEvent());
    TestEvent event = interceptor.eventRef.await(5000);
    assertNotNull("Event not dispatched", event);
  }

}
