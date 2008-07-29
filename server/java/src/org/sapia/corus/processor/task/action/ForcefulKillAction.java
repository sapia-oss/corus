package org.sapia.corus.processor.task.action;

import java.io.IOException;
import java.util.Date;

import org.sapia.corus.LogicException;
import org.sapia.corus.port.PortManager;
import org.sapia.corus.processor.NativeProcessFactory;
import org.sapia.corus.processor.Process;
import org.sapia.corus.processor.ProcessDB;
import org.sapia.corus.taskmanager.Action;
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
public class ForcefulKillAction implements Action{
  
  private TCPAddress _dynSvr;
  private int        _httpPort;
  private String    _requestor;
  private ProcessDB _db;
  private String    _corusPid;
  private long      _restartIntervalMillis;
  private PortManager _ports;
  
  public ForcefulKillAction(TCPAddress dynSvrAddress, int httpPort, String requestor, ProcessDB db, String corusPid, long restartIntervalMillis, PortManager ports){
    _dynSvr = dynSvrAddress;
    _httpPort = httpPort;
    _requestor = requestor;
    _db = db;
    _corusPid = corusPid;
    _restartIntervalMillis = restartIntervalMillis;
    _ports = ports;
  }
  
  /**
   * @see org.sapia.corus.taskmanager.Action#execute(org.sapia.taskman.TaskContext)
   */
  public boolean execute(TaskContext ctx) {
    boolean killSuccessful = false;

    try {
      
      Process process = _db.getActiveProcesses().getProcess(_corusPid);
      

      ctx.getTaskOutput().warning("Process " + process.getProcessID() +
                    " did not confirm kill: " + process + "; requestor: " + _requestor);

      // try forcefull kill if OS pid not null...
      if (process.getOsPid() != null) {
        try {
          doNativeKill(ctx, process);
          killSuccessful = true;
        } catch (IOException e) {
          ctx.getTaskOutput().warning("Error performing OS kill on process " +
                        process.getOsPid());
          ctx.getTaskOutput().error(e);
        }
      } else {
        ctx.getTaskOutput().warning("Process " + _corusPid +
                      " is stalled but could not be killed");
      }
      
      process.releasePorts(_ports);

      if(!ActionFactory.newCleanupProcessAction(_db, process).execute(ctx)){
        ctx.getTaskOutput().warning("Process " + process.getProcessID() + " will not be restarted");
        return false;
      }

      // if shutdown was initiated by Corus server, restart process
      // automatically (if restarted interval threshold is respected)
      if (_requestor.equals(Process.KILL_REQUESTOR_SERVER) && _restartIntervalMillis > 0) {
        ctx.getTaskOutput().debug("Preparing for restart");
        ctx.getTaskOutput().debug("Process creation time: " + new Date(process.getCreationTime()));
        ctx.getTaskOutput().debug("Current time: " + new Date());
        ctx.getTaskOutput().debug("Restart interval: " + (double)(_restartIntervalMillis/1000) + " seconds");
        // if no OS pid, then process could not be forcefully killed...
        if (process.getOsPid() == null) {
          ctx.getTaskOutput().warning("Not restarting process: " + process.getProcessID() +
                        "; did not confirm shutdown");
          ctx.getTaskOutput().warning("Could not be forcefully killed (because it does not have an OS pid)");
          ctx.getTaskOutput().warning("Might be stalled... Make sure that you do not have a process in limbo");
          onNoOsPid();
        } else if (((System.currentTimeMillis() - process.getCreationTime()) < _restartIntervalMillis)) {
          ctx.getTaskOutput().warning("Process will not be restarted; not enough time since last restart");
          onRestartThresholdInvalid();
        } else {
          ctx.getTaskOutput().warning("Restarting Process: " + process);
          killSuccessful = ActionFactory.newRestartVmAction(_dynSvr, _httpPort, _db, process, _ports).execute(ctx);
          onRestarted();
        }
      } else {
        ctx.getTaskOutput().warning("Process " + process.getProcessID() + " terminated");
      }
    } catch (LogicException e) {
      ctx.getTaskOutput().error(e);
    }

    return killSuccessful;
  }
  
  protected void doNativeKill(TaskContext ctx, Process proc) throws IOException{
    NativeProcessFactory.newNativeProcess().kill(ctx, proc.getOsPid());    
  }
  
  protected void onNoOsPid(){}
  
  protected void onRestartThresholdInvalid(){}
  
  protected void onRestarted(){}

}
