package org.sapia.corus.taskmanager.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.sapia.corus.client.common.range.IntRange;
import org.sapia.corus.taskmanager.core.FutureResult;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskManager;
import org.sapia.ubik.util.Func;

@RunWith(MockitoJUnitRunner.class)
public class CompositeTaskTest {
  
  @Mock
  private TaskManager          taskMan;
  @Mock
  private TaskExecutionContext taskContext;
  @Mock
  private FutureResult<Void>   futureResult;
  
  private CompositeTask        task;
  private List<Task<?, ?>>     childTasks;
  
  @Before
  public void setUp() throws Exception {
    task = new CompositeTask();
    
    childTasks = IntRange.forLength(5).asList(new Func<Task<?, ?>, Integer>() {
      @Override
      public Task<?, ?> call(Integer index) {
        Task<?, ?> childTask = mock(Task.class);
        task.add(childTask);
        return childTask;
      }
    });
    
    when(taskContext.getTaskManager()).thenReturn(taskMan);
    
    doAnswer(new Answer<FutureResult<Void>>() {
      @Override
      public FutureResult<Void> answer(InvocationOnMock invocation) throws Throwable {
        return futureResult;
      }
    }).when(taskMan).executeAndWait(any(Task.class), any(Object.class));
  }

  @Test
  public void testGetChildTasks() {
    assertTrue(childTasks.containsAll(task.getChildTasks()));
  }

  @Test
  public void testGetTaskCount() {
    assertEquals(childTasks.size(), task.getChildTasks().size());
  }

  @Test
  public void testExecute() throws Throwable {
    task.execute(taskContext, null);
    verify(futureResult, times(5)).get();
  }

}
