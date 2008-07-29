package org.sapia.corus.processor.task.action;

import org.sapia.corus.port.PortManager;
import org.sapia.corus.processor.Process;
import org.sapia.corus.processor.ProcessDB;
import org.sapia.corus.processor.task.KillTask;
import org.sapia.corus.taskmanager.Action;
import org.sapia.taskman.PeriodicTaskDescriptor;
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
public class KillProcessAction implements Action{
  
  private TCPAddress _dynSvr;
  private int        _httpPort;
  private String _requestor;
  private ProcessDB _db;
  private Process _proc;
  private long    _killInterval, _restartInterval;
  private PortManager _ports;
  
  public KillProcessAction(TCPAddress dynSvrAddress, 
                           int httpPort,
                           String requestor, 
                           ProcessDB db, 
                           Process proc,
                           long killIntervalMillis,
                           long restartIntervalMillis,
                           PortManager ports){
    _dynSvr = dynSvrAddress;
    _httpPort = httpPort;
    _requestor = requestor;
    _db = db;
    _proc = proc;
    _killInterval = killIntervalMillis;
    _restartInterval = restartIntervalMillis;
    _ports = ports;
  }
  /**
   * @see org.sapia.corus.taskmanager.Action#execute(org.sapia.taskman.TaskContext)
   */
  public boolean execute(TaskContext ctx) {
    KillTask kill = new KillTask(_dynSvr, _httpPort, _requestor, _proc.getProcessID(), _db,
                                 _proc.getMaxKillRetry(),
                                 _restartInterval, _ports);
    PeriodicTaskDescriptor desc = new PeriodicTaskDescriptor("KillProcessTask", _killInterval, kill);
    ctx.execTaskFor(desc);
    return true;
  }

}
