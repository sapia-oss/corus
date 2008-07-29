package org.sapia.corus.processor.task.action;

import java.io.File;
import java.io.IOException;

import org.sapia.console.CmdLine;
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
public interface ActionProvider {
  
  public CleanupProcessAction newCleanupProcessAction(ProcessDB db, Process proc);
  
  public KillConfirmedAction newKillConfirmedAction(ProcessDB db, Process proc, PortManager ports);
  
  public MakeProcessDirAction newMakeProcessDirAction(ProcessInfo info);
  
  public RestartVmAction newRestartVmAction(TCPAddress dynSvrAddress, int httpPort, ProcessDB db, Process proc, PortManager ports);
  
  public ExecProcessAction newExecProcessAction(TCPAddress dynSvrAddress, int httpPort, ProcessInfo info, PortManager ports) throws IOException;
  
  public ExecCmdLineAction newExecCmdLineAction(File processDir, CmdLine cmdLine, Process proc);
  
  public KillProcessAction newKillProcessAction(TCPAddress dynSvrAddress, int httpPort, String requestor, ProcessDB db, Process proc, long killIntervalMillis, long restartIntervalMillis, PortManager ports);
  
  public AttemptKillAction newAttemptKillAction(String requestor, ProcessDB db, Process proc, int currentRetryCount);
  
  public ForcefulKillAction newForcefulKillAction(TCPAddress dynSvrAddress, int httpPort, String requestor, ProcessDB db, String corusPid, long restartIntervalMillis, PortManager ports);
}
