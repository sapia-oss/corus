package org.sapia.corus.http.rest;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.sapia.corus.client.rest.async.AsyncTask;
import org.sapia.corus.taskmanager.core.BackgroundTaskConfig;
import org.sapia.corus.taskmanager.core.FutureResult;
import org.sapia.corus.taskmanager.core.SequentialTaskConfig;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskConfig;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskManager;
import org.sapia.corus.taskmanager.core.Throttle;
import org.sapia.corus.taskmanager.core.ThrottleKey;

@RunWith(MockitoJUnitRunner.class)
public class TaskManagerExecutionProviderTest {
  
  private TaskManager tasks;
 
  @Mock
  private AsyncTask   task;
  
  private TaskManagerExecutionProvider executor;
  

  @Before
  public void setUp() throws Exception {
    tasks = new TaskManager() {
      
      @Override
      public void registerThrottle(ThrottleKey key, Throttle throttle) {
      }
      
      @Override
      public <R, P> void executeBackground(Task<R, P> task, P param,
          BackgroundTaskConfig config) {
      }
      
      @Override
      public <R, P> FutureResult<R> executeAndWait(Task<R, P> task, P param,
          TaskConfig conf) {
        return null;
      }
      
      @Override
      public <R, P> FutureResult<R> executeAndWait(Task<R, P> task, P param) {
        return null;
      }
      
      @Override
      public <R, P> void execute(Task<R, P> task, P param, SequentialTaskConfig conf) {
      }
      
      @Override
      public <R, P> void execute(Task<R, P> task, P param) {
        try {
          task.execute(mock(TaskExecutionContext.class), null);
        } catch (Throwable e) {
          fail(e.getMessage());
        }
      }
    };
    
    executor = new TaskManagerExecutionProvider(tasks);

  }

  @Test
  public void testScheduleForExecution() {
    executor.scheduleForExecution(task);
    verify(task).execute();
    verify(task).releaseResources();
  }

}
