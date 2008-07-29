package org.sapia.corus.processor.task.action;

import org.sapia.corus.processor.Process;
import org.sapia.corus.processor.ProcessDB;
import org.sapia.corus.processor.TestProcessDB;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class CleanupProcessActionTest extends BaseActionTest{
  
  /**
   * @param arg0
   */
  public CleanupProcessActionTest(String arg0) {
    super(arg0);
  }
  
  public void testExecute(){
    ProcessDB        db   = new TestProcessDB();
    Process          proc = new Process(_dist);
    db.getActiveProcesses().addProcess(proc);
    CleanupProcessAction clean = new CleanupProcessAction(db, proc);
    super.assertTrue(clean.execute(_ctx));
    super.assertTrue(!db.getActiveProcesses().containsProcess(proc.getProcessID()));    
  }

}
