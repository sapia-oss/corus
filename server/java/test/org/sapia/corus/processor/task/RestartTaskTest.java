package org.sapia.corus.processor.task;

import org.sapia.corus.deployer.DistributionStore;
import org.sapia.corus.deployer.config.Distribution;
import org.sapia.corus.deployer.config.ProcessConfig;
import org.sapia.corus.port.TestPortManager;
import org.sapia.corus.processor.DistributionInfo;
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
public class RestartTaskTest extends BaseTaskTest{
  /**
   * @param arg0
   */
  public RestartTaskTest(String arg0) {
    super(arg0);
  }
  
  public void testRestart() throws Exception{
    ProcessDB        db   = new TestProcessDB();
    Distribution dist = new Distribution();
    dist.setName("test");
    dist.setVersion("1.0");
    ProcessConfig conf = new ProcessConfig();
    conf.setName("testVm");
    dist.addProcess(conf);
    DistributionStore store = new DistributionStore();
    store.addDistribution(dist);
    DistributionInfo info = new DistributionInfo("test", "1.0", "test", "testVm");
    Process          proc = new Process(info);
    db.getActiveProcesses().addProcess(proc);
    RestartTask restart = new RestartTask(new TCPAddress("localhost", 1), 8080, Process.KILL_REQUESTOR_PROCESS, db, store, proc.getProcessID(), 3, new TestPortManager());
    _tm.execSyncTask("restart", restart);
    proc.confirmKilled();    
    _tm.execSyncTask("restart", restart);    
  
  }

}
