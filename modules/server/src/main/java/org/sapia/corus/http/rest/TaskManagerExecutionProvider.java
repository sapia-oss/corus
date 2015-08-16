package org.sapia.corus.http.rest;

import org.sapia.corus.client.rest.async.AsyncTask;
import org.sapia.corus.http.rest.AsynchronousCompletionServiceImpl.TaskExecutionProvider;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskManager;

/**
 * Implements the {@link TaskExecutionProvider} interface over a {@link TaskManager}.
 * 
 * @author yduchesne
 *
 */
public class TaskManagerExecutionProvider implements TaskExecutionProvider {

  private TaskManager tasks;
  
  public TaskManagerExecutionProvider(TaskManager tasks) {
    this.tasks = tasks;
  }
  
  @Override
  public void scheduleForExecution(final AsyncTask task) {
    tasks.execute(new Task<Void, Void>() {
      @Override
      public Void execute(TaskExecutionContext ctx, Void param)
          throws Throwable {
        try {
          task.execute();
        } finally {
          task.releaseResources();
        }
        return null;
      }
    }, null);
  }
}
