package org.sapia.corus.processor.task.action;

import org.sapia.corus.processor.Process;
import org.sapia.corus.processor.TestProcessDB;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class AttemptKillActionTest extends BaseActionTest{
  
  public AttemptKillActionTest(String name){
    super(name);
  }
  
  public void testExecute(){
    Process          proc = new Process(_dist);
    proc.setMaxKillRetry(3);
    AttemptKillAction kill = new AttemptKillAction(Process.KILL_REQUESTOR_ADMIN, new TestProcessDB(), proc, 1);
    super.assertTrue(!kill.execute(_ctx));
    proc.confirmKilled();
    super.assertTrue(kill.execute(_ctx));    
  }

}
