package org.sapia.corus.processor.task.action;

import org.sapia.corus.port.TestPortManager;
import org.sapia.corus.processor.Process;
import org.sapia.corus.processor.ProcessDB;
import org.sapia.taskman.TaskContext;
import org.sapia.ubik.net.TCPAddress;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class TestRestartVmAction extends RestartVmAction{
  
  /**
   * @param addr
   * @param db
   * @param proc
   */
  public TestRestartVmAction(TCPAddress addr, ProcessDB db, Process proc) throws Exception{
    super(addr, 8080, db, proc, new TestPortManager());
  }
  
  /**
   * @see org.sapia.corus.taskmanager.Action#execute(org.sapia.taskman.TaskContext)
   */
  public boolean execute(TaskContext ctx) {
    return true;
  }
}
