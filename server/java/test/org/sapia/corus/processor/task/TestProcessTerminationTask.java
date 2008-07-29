package org.sapia.corus.processor.task;

import org.sapia.corus.port.TestPortManager;
import org.sapia.corus.processor.ProcessDB;

import org.sapia.taskman.TaskContext;
import org.sapia.ubik.net.TCPAddress;


/**
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class TestProcessTerminationTask extends ProcessTerminationTask {
  int onExec     = 0;
  int onMaxRetry = 0;

  public TestProcessTerminationTask(String requestor, String corusPid,
                                    ProcessDB db, int maxRetry) throws Exception{
    super(new TCPAddress("localhost", 33000), 8080, requestor, corusPid, db, maxRetry, new TestPortManager());
  }
  
  /**
   * @see org.sapia.taskman.RetryTask#exec(org.sapia.taskman.TaskContext)
   */
  public void exec(TaskContext arg0) {
    super.exec(arg0);
  }

  protected void cleanupProcess(Process proc, TaskContext ctx) {
  }
  
  protected void onExec(TaskContext ctx) {
    onExec++;
  }

  protected boolean onMaxRetry(TaskContext ctx) {
    onMaxRetry++;

    return false;
  }
  
  /**
   * @see org.sapia.corus.processor.task.ProcessTerminationTask#onKillConfirmed(org.sapia.taskman.TaskContext)
   */
  protected void onKillConfirmed(TaskContext ctx) {

  }
}
