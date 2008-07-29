package org.sapia.corus.processor.task.action;

import org.sapia.corus.processor.Process;
import org.sapia.corus.processor.ProcessDB;
import org.sapia.corus.processor.TestProcessDB;
import org.sapia.ubik.net.TCPAddress;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ForcefulKillActionTest extends BaseActionTest{
  
  /**
   * @param arg0
   */
  public ForcefulKillActionTest(String arg0) {
    super(arg0);
  }
  
  public void testNoPid() throws Exception{
    ProcessDB        db   = new TestProcessDB();
    Process          proc = new Process(_dist);
    db.getActiveProcesses().addProcess(proc);
    TestForcefulKillAction kill = new TestForcefulKillAction(new TCPAddress("localhost", 8080), Process.KILL_REQUESTOR_SERVER, db, proc.getProcessID(), 1);
    super.assertTrue(!kill.execute(_ctx));
    super.assertTrue(kill.noPid);
  }
  
  public void testRestarted() throws Exception{
    ProcessDB        db   = new TestProcessDB();
    Process          proc = new Process(_dist);
    proc.setOsPid("1234");
    db.getActiveProcesses().addProcess(proc);
    Thread.sleep(200);
    TestForcefulKillAction kill = new TestForcefulKillAction(new TCPAddress("localhost", 8080), Process.KILL_REQUESTOR_SERVER, db, proc.getProcessID(), 100);
    
    super.assertTrue(kill.execute(_ctx));
    super.assertTrue(kill.nativeKill);    
    super.assertTrue(kill.restart);
  }
  
  public void testNoServer() throws Exception{
    ProcessDB        db   = new TestProcessDB();
    Process          proc = new Process(_dist);
    proc.setOsPid("1234");
    db.getActiveProcesses().addProcess(proc);
    Thread.sleep(200);
    TestForcefulKillAction kill = new TestForcefulKillAction(new TCPAddress("localhost", 8080), Process.KILL_REQUESTOR_ADMIN, db, proc.getProcessID(), 1);
    
    super.assertTrue(kill.execute(_ctx));
    super.assertTrue(kill.nativeKill);    
    super.assertTrue(!kill.restart);
  }    
  
  public void testRestartInvalid() throws Exception{
    ProcessDB        db   = new TestProcessDB();
    Process          proc = new Process(_dist);
    proc.setOsPid("1234");
    db.getActiveProcesses().addProcess(proc);
    Thread.sleep(200);
    TestForcefulKillAction kill = new TestForcefulKillAction(new TCPAddress("localhost", 8080), Process.KILL_REQUESTOR_SERVER, db, proc.getProcessID(), 1000);
    
    super.assertTrue(kill.execute(_ctx));
    super.assertTrue(kill.nativeKill);    
    super.assertTrue(kill.restartInvalid);
  }
  

}
