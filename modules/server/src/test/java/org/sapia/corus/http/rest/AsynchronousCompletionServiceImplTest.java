package org.sapia.corus.http.rest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.rest.async.AsyncTask;
import org.sapia.corus.http.rest.AsynchronousCompletionServiceImpl.AsyncTaskWrapper;
import org.sapia.ubik.util.SysClock.MutableClock;
import org.sapia.ubik.util.TimeValue;

@RunWith(MockitoJUnitRunner.class)
public class AsynchronousCompletionServiceImplTest {

  @Mock
  private TaskManagerExecutionProvider  executor;
 
  private MutableClock                  clock;

  @Mock
  private AsyncTask                     task;

  private Map<String, AsyncTaskWrapper> tasks;

  private TimeValue                     timeout;
  
  private AsynchronousCompletionServiceImpl service;
  
  @Before
  public void setUp() throws Exception {
    clock   = MutableClock.getInstance();
    tasks   = new HashMap<String, AsynchronousCompletionServiceImpl.AsyncTaskWrapper>();
    timeout = TimeValue.createMillis(100);
    service = new AsynchronousCompletionServiceImpl(tasks, executor, timeout);
    service.setClock(clock);
    
    when(task.isRunning()).thenReturn(true);
  }

  @Test
  public void testRegisterForExecution() {
    String token = service.registerForExecution(task);
    
    service.getAsyncTask(token, AsyncTask.class);

    assertTrue(tasks.containsKey(token));
    verify(executor).scheduleForExecution(task);
  }

  @Test
  public void testGetAsyncTask() {
  }

  @Test
  public void testUnregister() {
    String token = service.registerForExecution(task);
    
    service.unregister(token);
    
    assertFalse(tasks.containsKey(token));
   }

  @Test
  public void testShutdown() {
    service.registerForExecution(task);
    
    service.shutdown();
    
    verify(task).terminate();
  }
  
  @Test
  public void testShutdown_not_running() {
    service.registerForExecution(task);
    
    service.shutdown();
    
    verify(task).terminate();
  }

  @Test
  public void testFlushStaleTasks() {
    service.registerForExecution(task);
    
    service.flushStaleTasks();
   
    verify(task, never()).terminate();
    
  }

  @Test
  public void testFlushStaleTasks_timed_out() {
    service.registerForExecution(task);
    clock.increaseCurrentTimeMillis(timeout.getValueInMillis() + 1);
    
    service.flushStaleTasks();
   
    verify(task).terminate();
  }
}
