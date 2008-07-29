package org.sapia.corus.processor.task.action;

import org.sapia.corus.processor.DistributionInfo;
import org.sapia.corus.taskmanager.TestTaskContext;
import org.sapia.corus.taskmanager.TestTaskOutput;
import org.sapia.taskman.TaskContext;
import org.sapia.taskman.TestTaskManager;

import junit.framework.TestCase;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class BaseActionTest extends TestCase{
  
  protected DistributionInfo _dist;
  protected TaskContext      _ctx;
  
  /**
   * @param arg0
   */
  public BaseActionTest(String arg0) {
    super(arg0);
  }
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    _dist = new DistributionInfo("test", "1.0", "test", "testVm");
    TestTaskManager man = new TestTaskManager();
    _ctx = new TestTaskContext(new TestTaskOutput(), man);
    ActionFactory._provider = new TestActionProvider();
  }
  
  public void testNoop(){}

}
