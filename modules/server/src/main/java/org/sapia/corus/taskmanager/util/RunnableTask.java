package org.sapia.corus.taskmanager.util;

import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContextImpl;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;

/**
 * A {@link Task} that implements the {@link Runnable} interface. Inheriting classes
 * need only implementing the {@link #run()} method.
 * 
 * @author yduchesne
 *
 */
public abstract class RunnableTask extends Task<Void, Void> implements Runnable {
  
  private TaskExecutionContext context;
  
  @Override
  public Void execute(TaskExecutionContext ctx, Void param) throws Throwable {
    context = ctx;
    run();
    return null;
  }
  
  @Override
  public abstract void run();

  
  /**
   * @return this instance's {@link TaskExecutionContextImpl}.
   */
  protected TaskExecutionContext context() {
    return context;
  }
  
}
