package org.sapia.corus.processor.task.action;

import java.io.File;
import java.util.Properties;

import org.sapia.console.CmdLine;
import org.sapia.corus.port.PortManager;
import org.sapia.corus.processor.Process;
import org.sapia.corus.processor.ProcessInfo;
import org.sapia.corus.processor.ProcessDB;
import org.sapia.ubik.net.TCPAddress;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class TestActionProvider extends DefaultActionProvider{
  
  /**
   * @see org.sapia.corus.processor.task.action.DefaultActionProvider#newExecCmdLineAction(org.sapia.console.CmdLine, org.sapia.corus.processor.Process)
   */
  public ExecCmdLineAction newExecCmdLineAction(
    File procDir,
    CmdLine cmdLine,
    Process proc) {
    return new TestExecCmdLineAction(procDir, cmdLine, proc);
  }
  
  /**
   * @see org.sapia.corus.processor.task.action.DefaultActionProvider#newRestartVmAction(org.sapia.ubik.net.TCPAddress, org.sapia.corus.processor.ProcessDB, org.sapia.corus.processor.Process)
   */
  public RestartVmAction newRestartVmAction(
    TCPAddress dynSvrAddress,
    int httpPort,
    ProcessDB db,
    Process proc, 
    PortManager ports) {
    try{
      return new TestRestartVmAction(dynSvrAddress, db, proc);
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }
  
  public ExecProcessAction newExecProcessAction(
    TCPAddress dynSvrAddress,
    int httpPort,
    ProcessInfo info, PortManager ports) {
    return new ExecProcessAction(dynSvrAddress, httpPort, info, new Properties(), ports);
  }  
  
  /**
   * @see org.sapia.corus.processor.task.action.DefaultActionProvider#newForcefulKillAction(org.sapia.ubik.net.TCPAddress, java.lang.String, org.sapia.corus.processor.ProcessDB, java.lang.String, long)
   */
  public ForcefulKillAction newForcefulKillAction(
    TCPAddress dynSvrAddress,
    int httpPort,
    String requestor,
    ProcessDB db,
    String corusPid,
    long restartIntervalMillis,
    PortManager ports) {
    

    try{
      return new TestForcefulKillAction(
        dynSvrAddress,
        requestor,
        db,
        corusPid,
        restartIntervalMillis);
    }catch(Exception e){
      throw new RuntimeException(e);
    }
  }
  
  
  
  
}
