package org.sapia.corus.client.rest.async;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class AsyncTaskSupportTest {

  private AsyncTaskSupport task;
  
  @Before
  public void setUp() throws Exception {

    task = new AsyncTaskSupport() {
      @Override
      protected void doTerminate() {
        
      }
      
      @Override
      protected void doExecute() {
      }
      
      @Override
      public void releaseResources() {
      }
    };
    
  }

  @Test
  public void testExecute() {
    task.execute();
    assertFalse(task.isRunning());
  }

  @Test
  public void testTerminate() {
    task.terminate();
    assertFalse(task.isRunning());
  }

}
