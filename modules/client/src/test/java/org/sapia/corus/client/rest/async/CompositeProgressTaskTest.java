package org.sapia.corus.client.rest.async;

import static org.junit.Assert.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.rest.ProgressResult;
import org.sapia.corus.client.services.http.HttpResponseFacade;

@RunWith(MockitoJUnitRunner.class)
public class CompositeProgressTaskTest {
  
  @Mock
  private ProgressCapableTask task1, task2;
  
  private CompositeProgressTask tasks;

  @Before
  public void setUp() throws Exception {
    tasks = new CompositeProgressTask() {
      @Override
      protected void triggerNextTask(ProgressCapableTask nextTask) {
        nextTask.execute();
      }
    };
    
    tasks.addTask(task1).addTask(task2);
    
    when(task1.getNextResult()).thenReturn(new ProgressResult(Arrays.asList("task1")));
    when(task2.getNextResult()).thenReturn(new ProgressResult(Arrays.asList("task2")));
    
  }

  @Test
  public void testTerminate() {
    tasks.execute();
    tasks.terminate();
  
    assertFalse(tasks.isRunning());
  }

  @Test
  public void testReleaseResources() throws Exception {
    tasks.execute();
    tasks.getNextResult();
    tasks.getNextResult();
    tasks.releaseResources();
  
    verify(task1).releaseResources();
    verify(task2).releaseResources();
  }
  
  @Test
  public void testReleaseResources_after_terminate() {
    tasks.execute();
    tasks.terminate();
    tasks.releaseResources();
  
    verify(task1).releaseResources();
  }

  @Test
  public void testIsRunning() {
    tasks.execute();
    
    assertTrue(tasks.isRunning());
  }

  @Test
  public void testExecute() {
    tasks.execute();
    
    verify(task1).execute();
    verify(task2, never()).execute();
  }

  @Test
  public void testGetNextResult() throws Exception {
    tasks.execute();
    
    ProgressResult r1 = tasks.getNextResult();
    assertEquals(HttpResponseFacade.STATUS_IN_PROGRESS, r1.getStatus());
    verify(task1).releaseResources();
    
    ProgressResult r2 = tasks.getNextResult();
    assertEquals(HttpResponseFacade.STATUS_OK, r2.getStatus());
    verify(task2, never()).terminate();
    verify(task2, never()).releaseResources();

  }

}
