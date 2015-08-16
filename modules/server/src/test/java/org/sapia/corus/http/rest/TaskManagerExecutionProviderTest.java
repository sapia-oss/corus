package org.sapia.corus.http.rest;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sapia.corus.client.rest.async.AsyncTask;
import org.sapia.corus.taskmanager.core.TaskManager;

@RunWith(MockitoJUnitRunner.class)
public class TaskManagerExecutionProviderTest {
  
  @Mock
  private TaskManager tasks;
 
  @Mock
  private AsyncTask   task;
  
  private TaskManagerExecutionProvider executor;
  

  @Before
  public void setUp() throws Exception {
    executor = new TaskManagerExecutionProvider(tasks);
  }

  @Test
  public void testScheduleForExecution() {
    executor.scheduleForExecution(task);
    verify(task).execute();
    verify(task).releaseResources();
  }

}
