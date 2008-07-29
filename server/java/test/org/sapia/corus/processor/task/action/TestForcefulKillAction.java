package org.sapia.corus.processor.task.action;

import java.io.IOException;
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
public class TestForcefulKillAction extends ForcefulKillAction{
  
  boolean nativeKill, noPid, restart, restartInvalid;
  
  /**
   * @param dynSvrAddress
   * @param requestor
   * @param db
   * @param corusPid
   * @param restartIntervalMillis
   */
  public TestForcefulKillAction(
    TCPAddress dynSvrAddress,
    String requestor,
    ProcessDB db,
    String corusPid,
    long restartIntervalMillis) throws Exception{
    super(dynSvrAddress, 8080, requestor, db, corusPid, restartIntervalMillis, new TestPortManager());
  }
  
  protected void doNativeKill(TaskContext ctx, Process proc) throws IOException {
    nativeKill = true;
  }

  protected void onNoOsPid() {
    noPid = true;
  }

  /**
   * @see org.sapia.corus.processor.task.action.ForcefulKillAction#onRestarted()
   */
  protected void onRestarted() {
    restart = true;
  }

  /**
   * @see org.sapia.corus.processor.task.action.ForcefulKillAction#onRestartThresholdInvalid()
   */
  protected void onRestartThresholdInvalid() {
    restartInvalid = true;
  }

}
