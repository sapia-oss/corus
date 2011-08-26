package org.sapia.corus.taskmanager.core;

/**
 * An instance of this interface allows task to output logging information.
 * <p/>
 * Each method corresponds to a specific log level. 
 */
public interface TaskLog {
  
  public void debug(Task<?, ?> task, String msg);
  
  public void info(Task<?, ?> task, String msg);
  
  public void warn(Task<?, ?> task, String msg);

  public void warn(Task<?, ?> task, String msg, Throwable err);

  public void error(Task<?, ?> task, String msg);
  
  public void error(Task<?, ?> task, String msg, Throwable err);
  
  public void close();

}
