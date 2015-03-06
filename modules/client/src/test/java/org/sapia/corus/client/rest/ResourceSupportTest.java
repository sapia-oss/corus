package org.sapia.corus.client.rest;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.sapia.corus.client.common.ProgressMsg;
import org.sapia.corus.client.common.ProgressQueue;
import org.sapia.corus.client.common.ProgressQueueImpl;
import org.sapia.corus.client.services.http.HttpResponseFacade;

public class ResourceSupportTest {

  private ResourceSupport res;
  
  @Before
  public void setUp() {
    res = new ResourceSupport() {
      
    };
  }
  
  @Test
  public void testProgress_error_throwable() {
    ProgressQueue queue = new ProgressQueueImpl();
    queue.addMsg(new ProgressMsg(new Exception("test"), ProgressMsg.ERROR));
    queue.close();
    
    ProgressResult r = res.progress(queue);
   
    assertEquals(HttpResponseFacade.STATUS_SERVER_ERROR, r.getStatus());
    assertNotNull(r.getThrowable());
  }

  @Test
  public void testProgress_error() {
    ProgressQueue queue = new ProgressQueueImpl();
    queue.addMsg(new ProgressMsg("test", ProgressMsg.ERROR));
    queue.close();
    
    ProgressResult r = res.progress(queue);
   
    assertEquals(HttpResponseFacade.STATUS_SERVER_ERROR, r.getStatus());
    assertNull(r.getThrowable());
  }
  
  @Test
  public void testProgress_ok_throwable() {
    ProgressQueue queue = new ProgressQueueImpl();
    queue.addMsg(new ProgressMsg(new Exception("test"), ProgressMsg.WARNING));
    queue.close();
    
    ProgressResult r = res.progress(queue);
   
    assertEquals(HttpResponseFacade.STATUS_OK, r.getStatus());
    assertNull(r.getThrowable());
  }
  
  @Test
  public void testProgress_ok_error() {
    ProgressQueue queue = new ProgressQueueImpl();
    queue.addMsg(new ProgressMsg("test", ProgressMsg.INFO));
    queue.close();
    
    ProgressResult r = res.progress(queue);
   
    assertEquals(HttpResponseFacade.STATUS_OK, r.getStatus());
    assertNull(r.getThrowable());
  }
}
