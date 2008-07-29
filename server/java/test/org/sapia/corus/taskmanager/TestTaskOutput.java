package org.sapia.corus.taskmanager;

import org.sapia.taskman.TaskOutput;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class TestTaskOutput implements TaskOutput{

  /**
   * @see org.sapia.taskman.TaskOutput#close()
   */
  public void close() {
  }

  /**
   * @see org.sapia.taskman.TaskOutput#debug(java.lang.Object)
   */
  public TaskOutput debug(Object arg0) {
    return this;
  }

  /**
   * @see org.sapia.taskman.TaskOutput#error(java.lang.Object, java.lang.Throwable)
   */
  public TaskOutput error(Object arg0, Throwable arg1) {
    return this;
  }

  /**
   * @see org.sapia.taskman.TaskOutput#error(java.lang.Object)
   */
  public TaskOutput error(Object arg0) {
    return this;
  }

  /**
   * @see org.sapia.taskman.TaskOutput#error(java.lang.Throwable)
   */
  public TaskOutput error(Throwable arg0) {
    return this;
  }

  /**
   * @see org.sapia.taskman.TaskOutput#info(java.lang.Object)
   */
  public TaskOutput info(Object arg0) {
    return this;
  }

  /**
   * @see org.sapia.taskman.TaskOutput#setTaskName(java.lang.String)
   */
  public void setTaskName(String arg0) {
  }

  /**
   * @see org.sapia.taskman.TaskOutput#warning(java.lang.Object)
   */
  public TaskOutput warning(Object arg0) {
    return this;
  }

}
