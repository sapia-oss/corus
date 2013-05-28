package org.sapia.corus.taskmanager.util;

import java.util.ArrayList;
import java.util.List;

import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;

/**
 * A composite {@link Task} that encapsulates other tasks. These tasks should not expect any parameters, and the
 * result that they return (if any) will be discarded by the instance of this class that encapsulates them.
 * 
 * @author yduchesne
 *
 */
public class CompositeTask extends Task<Void, Void> {
  
  private List<Task<?,?>> children = new ArrayList<Task<?,?>>();
  
  /**
   * @param task a {@link Task} to add to this instance.
   * @return this instance.
   */
  public CompositeTask add(Task<?, ?> task) {
    children.add(task);
    return this;
  }
  
  /**
   * @return this instance's {@link List} of child {@link Task}s.
   */
  public List<Task<?, ?>> getChildTasks() {
    return new ArrayList<Task<?,?>>(children);
  }
  
  /**
   * @return the number of sub-tasks that this instance encapsulates.
   */
  public int getTaskCount() {
    return children.size();
  }
  
  @Override
  public Void execute(TaskExecutionContext ctx, Void param) throws Throwable {
    for (Task<?, ?> t : children) {
      ctx.getTaskManager().executeAndWait(t, null);
    }
    return null;
  }

}
