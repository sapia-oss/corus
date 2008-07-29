package org.sapia.corus.processor.task.action;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.sapia.console.CmdLine;
import org.sapia.corus.CorusRuntime;
import org.sapia.corus.port.PortManager;
import org.sapia.corus.processor.Process;
import org.sapia.corus.processor.ProcessDB;
import org.sapia.corus.processor.ProcessInfo;
import org.sapia.ubik.net.TCPAddress;

/**
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class DefaultActionProvider implements ActionProvider{
  
  /**
   * @see org.sapia.corus.processor.task.action.ActionProvider#newCleanupProcessAction(org.sapia.corus.processor.ProcessDB, org.sapia.corus.processor.Process)
   */
  public CleanupProcessAction newCleanupProcessAction(
    ProcessDB db,
    Process proc) {
    return new CleanupProcessAction(db, proc);
  }
  
  /**
   * @see org.sapia.corus.processor.task.action.ActionProvider#newKillConfirmedAction(org.sapia.corus.processor.ProcessDB, org.sapia.corus.processor.Process)
   */
  public KillConfirmedAction newKillConfirmedAction(
    ProcessDB db,
    Process proc, PortManager ports) {
    return new KillConfirmedAction(db, proc, ports);
  }
  
  /**
   * @see org.sapia.corus.processor.task.action.ActionProvider#newMakeProcessDirAction(org.sapia.corus.processor.ProcessInfo)
   */
  public MakeProcessDirAction newMakeProcessDirAction(ProcessInfo info) {
    return new MakeProcessDirAction(info);
  }
  
  /**
   * @see org.sapia.corus.processor.task.action.ActionProvider#newRestartVmAction(org.sapia.ubik.net.TCPAddress, org.sapia.corus.processor.ProcessDB, org.sapia.corus.processor.Process)
   */
  public RestartVmAction newRestartVmAction(
    TCPAddress dynSvrAddress,
    int httpPort,
    ProcessDB db,
    Process proc,
    PortManager ports) {
    return new RestartVmAction(dynSvrAddress, httpPort, db, proc, ports);
  }
  
  /**
   * @see org.sapia.corus.processor.task.action.ActionProvider#newExecCmdLineAction(org.sapia.console.CmdLine, org.sapia.corus.processor.Process)
   */
  public ExecCmdLineAction newExecCmdLineAction(
    File procDir,      
    CmdLine cmdLine,
    Process proc) {
    return new ExecCmdLineAction(procDir, cmdLine, proc);
  }
  
  /**
   * @see org.sapia.corus.processor.task.action.ActionProvider#newExecProcessAction(org.sapia.ubik.net.TCPAddress, org.sapia.corus.processor.ProcessInfo)
   */
  public ExecProcessAction newExecProcessAction(
    TCPAddress dynSvrAddress,
    int httpPort,
    ProcessInfo info,
    PortManager ports) throws IOException{
    return new ExecProcessAction(dynSvrAddress, httpPort, info, CorusRuntime.getProcessProperties(), ports);
  }
  
  /**
   * @see org.sapia.corus.processor.task.action.ActionProvider#newKillProcessAction(TCPAddress, String, ProcessDB, Process, long, long)
   */
  public KillProcessAction newKillProcessAction(
    TCPAddress dynSvrAddress,
    int httpPort,
    String requestor,
    ProcessDB db,
    Process proc,
    long killIntervalMillis,
    long restartIntervalMillis,
    PortManager ports) {
    return new KillProcessAction(dynSvrAddress, httpPort, requestor, db, proc, killIntervalMillis, restartIntervalMillis, ports);
  }
  
  /**
   * @see org.sapia.corus.processor.task.action.ActionProvider#newForcefulKillAction(org.sapia.ubik.net.TCPAddress, java.lang.String, org.sapia.corus.processor.ProcessDB, java.lang.String, long)
   */
  public ForcefulKillAction newForcefulKillAction(
    TCPAddress dynSvrAddress,
    int httpPort,
    String requestor,
    ProcessDB db,
    String corusPid,
    long restartIntervalMillis,
    PortManager ports) {
    return new ForcefulKillAction(dynSvrAddress,httpPort, requestor, db, corusPid, restartIntervalMillis, ports);
  }
  
  /**
   * @see org.sapia.corus.processor.task.action.ActionProvider#newAttemptKillAction(java.lang.String, org.sapia.corus.processor.ProcessDB, org.sapia.corus.processor.Process, int)
   */
  public AttemptKillAction newAttemptKillAction(
    String requestor,
    ProcessDB db,
    Process proc,
    int currentRetryCount) {
    return new AttemptKillAction(requestor, db, proc, currentRetryCount);
  }
  
}
