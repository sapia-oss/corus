package org.sapia.corus.processor.task;

import org.sapia.corus.processor.task.action.ActionFactory;
import org.sapia.corus.processor.task.action.TestActionProvider;
import org.sapia.taskman.NullTaskManager;
import org.sapia.taskman.TaskManager;

import junit.framework.TestCase;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class BaseTaskTest extends TestCase{
  
  protected TaskManager _tm;
  
  /**
   * @param arg0
   */
  public BaseTaskTest(String arg0) {
    super(arg0);
  }
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    _tm = new NullTaskManager();
    if(!ActionFactory.hasProvider()){
      ActionFactory.setActionProvider(new TestActionProvider());
    }
  }
  
  public void testNoop(){}

}
