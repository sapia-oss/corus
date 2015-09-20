package org.sapia.corus.cloud.platform.workflow;

/**
 * Logging abstraction meant to be used in the context of workflow execution.
 * 
 * @author yduchesne
 *
 */
public interface WorkflowLog {

  // --------------------------------------------------------------------------
  // error
  
  public void error(String msg, Object...args);
  public void error(String msg);
  public void error(Throwable err);

  // --------------------------------------------------------------------------
  // warning
  
  public void warning(String msg, Object...args);
  public void warning(String msg);
  public void warning(Throwable err);
  
  // --------------------------------------------------------------------------
  // info
  
  public void info(String msg, Object...args);
  public void info(String msg);
  public void info(Throwable err);
  
  // --------------------------------------------------------------------------
  // verbose
  
  public void verbose(String msg, Object...args);
  public void verbose(String msg);
  public void verbose(Throwable err);

}
