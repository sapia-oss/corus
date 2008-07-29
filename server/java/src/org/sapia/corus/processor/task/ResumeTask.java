package org.sapia.corus.processor.task;

import java.io.IOException;

import org.sapia.corus.deployer.config.Distribution;
import org.sapia.corus.deployer.config.ProcessConfig;
import org.sapia.corus.port.PortManager;
import org.sapia.corus.processor.LockException;
import org.sapia.corus.processor.Process;
import org.sapia.corus.processor.ProcessDB;
import org.sapia.corus.processor.ProcessInfo;
import org.sapia.corus.processor.task.action.ActionFactory;
import org.sapia.taskman.Task;
import org.sapia.taskman.TaskContext;
import org.sapia.ubik.net.TCPAddress;


/**
 * This task resumes a suspended process.
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ResumeTask implements Task{
  private TCPAddress    _dynSvr;
  private int           _httpPort;
  private ProcessDB     _db;
  private Process       _process;
  private Distribution  _dist;
  private ProcessConfig _conf;
  private PortManager   _ports;

  public ResumeTask(TCPAddress dynServerAddress, int httpPort, ProcessDB db, Process proc,
                    Distribution dist, ProcessConfig conf, PortManager ports)
             throws LockException {
    _dynSvr  = dynServerAddress;
    _process = proc;
    _dist    = dist;
    _conf    = conf;
    _db      = db;
    _httpPort = httpPort;
    _ports = ports;
    proc.acquireLock(this);
  }

  public void exec(TaskContext ctx) {
    _process.touch();
    try{
      if(ActionFactory.newExecProcessAction(_dynSvr, _httpPort, _db, new ProcessInfo(_process, _dist, _conf, true), _ports).execute(ctx)){
        _db.getSuspendedProcesses().removeProcess(_process.getProcessID());
        _process.setStatus(Process.ACTIVE);
        _db.getActiveProcesses().addProcess(_process);
      }
      else{
        ctx.getTaskOutput().error("Process: " + _process.getProcessID() + " will not be resumed");
      }
    }catch(IOException e){
      ctx.getTaskOutput().error("Error restarting process: " + _process.getProcessID(), e);
    }
  }
}
