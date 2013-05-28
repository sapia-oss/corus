package org.sapia.corus.taskmanager.core;

import org.sapia.corus.core.ServerContext;
import org.sapia.corus.core.ServerContextImpl;

public interface TaskExecutionContext {

  /**
   * @return the {@link Task} associated to this instance.
   */
  public abstract Task<?, ?> getTask();

  /**
   * @return this instance's {@link TaskLog}
   */
  public abstract TaskLog getLog();

  /**
   * @return this instance's {@link ServerContextImpl}
   */
  public abstract ServerContext getServerContext();

  /**
   * @return this instance's {@link TaskManager}
   */
  public abstract TaskManager getTaskManager();

  public abstract void debug(String msg);

  public abstract void info(String msg);

  public abstract void warn(String msg);

  public abstract void warn(String msg, Throwable err);

  public abstract void error(String msg);

  public abstract void error(String msg, Throwable err);

  public abstract void error(Throwable err);

  public abstract void close();

}