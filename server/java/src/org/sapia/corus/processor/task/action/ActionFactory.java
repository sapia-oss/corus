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
public class ActionFactory {
  static ActionProvider _provider;

  public static CleanupProcessAction newCleanupProcessAction(ProcessDB db, Process proc){
    return provider().newCleanupProcessAction(db, proc);
  }
  
  public static KillConfirmedAction newKillConfirmedAction(ProcessDB db, Process proc, PortManager ports){
    return provider().newKillConfirmedAction(db, proc, ports);
  }
  
  public static MakeProcessDirAction newMakeProcessDirAction(ProcessInfo info){
    return provider().newMakeProcessDirAction(info);
  }
  
  public static RestartVmAction newRestartVmAction(TCPAddress dynSvrAddress, int httpPort, ProcessDB db, Process proc, PortManager ports){
    return provider().newRestartVmAction(dynSvrAddress, httpPort, db, proc, ports);
  }
  
  public static ExecCmdLineAction newExecCmdLineAction(File procDir, CmdLine cmdLine, Process proc){
    return provider().newExecCmdLineAction(procDir, cmdLine, proc);
  }
  
  public static ExecProcessAction newExecProcessAction(TCPAddress dynSvrAddress, int httpPort, ProcessDB db, ProcessInfo info, PortManager ports) throws IOException{
    return provider().newExecProcessAction(dynSvrAddress, httpPort, info, ports);
  } 
  
  public static KillProcessAction newKillProcessAction(
      TCPAddress dynSvrAddress,
      int httpPort,
      String requestor,
      ProcessDB db,
      Process proc,
      long killIntervalMillis,
      long restartIntervalMillis,
      PortManager ports) {
    return provider().newKillProcessAction(dynSvrAddress, httpPort, requestor, db, proc, killIntervalMillis, restartIntervalMillis, ports);
  }
  
  public static ForcefulKillAction newForcefulKillAction(TCPAddress dynSvrAddress, int httpPort, String requestor, ProcessDB db, String corusPid, long restartIntervalMillis, PortManager ports){
    return provider().newForcefulKillAction(dynSvrAddress, httpPort, requestor, db, corusPid, restartIntervalMillis, ports);
  }
  
  public static AttemptKillAction newAttemptKillAction(String requestor, ProcessDB db, Process proc, int currentRetryCount){
    return provider().newAttemptKillAction(requestor, db, proc, currentRetryCount);
  }  
  
  
  
  public static void setActionProvider(ActionProvider provider){
    if(_provider != null){
      throw new IllegalStateException("Provider already set");
    }
    _provider = provider;
  }
  
  public static boolean hasProvider(){
    return _provider != null;
  }
  
  private static ActionProvider provider(){
    if(_provider == null){
      _provider = new DefaultActionProvider();
    }
    return _provider;
  }
  
}
