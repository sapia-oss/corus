package org.sapia.corus.taskmanager.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.sapia.corus.taskmanager.core.FutureResult;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;

/**
 * A composite {@link Task} that encapsulates other tasks. These tasks should
 * not expect any parameters, and the result that they return (if any) will be
 * discarded by the instance of this class that encapsulates them.
 * 
 * @author yduchesne
 * 
 */
public class CompositeTask extends Task<Void, Void> {

  private List<Task<?, ?>> children = new ArrayList<Task<?, ?>>();
  private List<Long> executionTimeout = new ArrayList<>();

  /**
   * @param task a {@link Task} to add to this instance.
   * @param timeout The execution timeout of this task in milliseconds.
   * @return this instance.
   */
  public CompositeTask add(Task<?, ?> task, long timeout) {
    children.add(task);
    executionTimeout.add(timeout);
    return this;
  }

  /**
   * @return this instance's {@link List} of child {@link Task}s.
   */
  public List<Task<?, ?>> getChildTasks() {
    return new ArrayList<Task<?, ?>>(children);
  }

  /**
   * @return the number of sub-tasks that this instance encapsulates.
   */
  public int getTaskCount() {
    return children.size();
  }

  @Override
  public Void execute(TaskExecutionContext ctx, Void param) throws Throwable {
    for (int i = 0; i < children.size(); i++) {
      Task<?, ?> task = children.get(i);
      long timeout = executionTimeout.get(i);
      
      ctx.debug("Executing nested task synchronously: " + task.getName());
      FutureResult<?> result = ctx.getTaskManager().executeAndWait(task, null);
      result.get(timeout);
      if (!result.isCompleted()) {
        throw new TimeoutException("Execution of child task " + task.getName() + " expired before completion (timeout=" + timeout + ")");
      }
    }

    return null;
  }

}
