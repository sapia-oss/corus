package org.sapia.corus.processor.task;

import java.util.List;

import org.sapia.corus.port.PortManager;
import org.sapia.corus.processor.Process;
import org.sapia.corus.processor.ProcessDB;
import org.sapia.corus.processor.task.action.ActionFactory;
import org.sapia.taskman.Task;
import org.sapia.taskman.TaskContext;
import org.sapia.ubik.net.TCPAddress;


/**
 * This task ensures that all external processes are up and running. It determines
 * so by checking the time at which each process last polled its Corus
 * server. This task is ran continuously at a predefined interval.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ProcessCheckTask implements Task{
  
  private ProcessDB  _db;
  private TCPAddress _addr;
  private int        _httpPort;
  private long       _vmTimeout, _killInterval, _restartInterval;
  private PortManager _ports;

  /**
   * Constructor for VmCheckTask.
   *
   * @param db a <code>ProcessDB</code>.
   * @param intervalSeconds an interval, in seconds.
   * @param vmTimeout a timeout, in millis.
   */
  public ProcessCheckTask(TCPAddress dynSvrAddress, int httpPort, ProcessDB db,
                          long killIntervalMillis, long vmTimeoutMillis, 
                          long restartIntervalMillis, PortManager ports) {
    _killInterval  = killIntervalMillis;
    _addr      = dynSvrAddress;
    _httpPort  = httpPort;
    _db        = db;
    _vmTimeout = vmTimeoutMillis;
    _restartInterval = restartIntervalMillis;
    _ports = ports;
  }
  
  /**
   * @see org.sapia.taskman.Task#exec(org.sapia.taskman.TaskContext)
   */
  public void exec(TaskContext ctx) {
    ctx.getTaskOutput().debug("Checking for stale processes...");

    List    processes = _db.getActiveProcesses().getProcesses();
    Process proc;

    for (int i = 0; i < processes.size(); i++) {
      proc = (Process) processes.get(i);

      if ((proc.getStatus() == Process.ACTIVE) &&
            proc.isTimedOut(_vmTimeout)) {
        if (proc.isLocked()) {
          ctx.getTaskOutput().warning("Process timed out but locked, probably terminating or restarting: " +
                        proc);
        } else {
          proc.setStatus(Process.KILL_REQUESTED);
          ctx.getTaskOutput().warning("Process timed out - ordering kill: " + proc);
          ActionFactory.newKillProcessAction(_addr, _httpPort, Process.KILL_REQUESTOR_SERVER, _db, proc, _killInterval, _restartInterval, _ports).execute(ctx);
          onTimeout();
        }
      } else if (proc.getStatus() == Process.KILL_CONFIRMED) {
        // will cleanup process dir, remove process from 
        // process store.
        if (!proc.isLocked()) {
          ActionFactory.newKillConfirmedAction(_db, proc, _ports).execute(ctx);
          //ActionFactory.newKillProcessAction(_addr, Process.KILL_REQUESTOR_PROCESS, _db, proc, _killInterval).execute(ctx);
        }
      }
    }

    ctx.getTaskOutput().debug("Stale process check finished");
  }
  
  protected void onTimeout(){}
}
