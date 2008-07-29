package org.sapia.corus.processor.task;

import java.io.IOException;

import org.sapia.corus.LogicException;
import org.sapia.corus.admin.CommandArg;
import org.sapia.corus.admin.CommandArgParser;
import org.sapia.corus.deployer.DistributionStore;
import org.sapia.corus.deployer.config.Distribution;
import org.sapia.corus.deployer.config.ProcessConfig;
import org.sapia.corus.port.PortManager;
import org.sapia.corus.processor.Process;
import org.sapia.corus.processor.ProcessDB;
import org.sapia.corus.processor.ProcessInfo;
import org.sapia.corus.processor.task.action.ActionFactory;
import org.sapia.taskman.Task;
import org.sapia.taskman.TaskContext;
import org.sapia.ubik.net.TCPAddress;


/**
 * This task restarts a given process (it terminates the process and restarts
 * it immediately).
 * 
 * @author Yanick Duchesne
 *
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class RestartTask extends ProcessTerminationTask {
  private DistributionStore _dists;

  public RestartTask(TCPAddress dynSvr, int httpPort, String requestor, ProcessDB db,
                     DistributionStore dists, String corusPid, int maxRetry, PortManager ports) {
    super(dynSvr, httpPort, requestor, corusPid, db, maxRetry, ports);
    _dists = dists;
  }
  
  /**
   * @see org.sapia.corus.processor.task.ProcessTerminationTask#onExec(org.sapia.taskman.TaskContext)
   */
  protected void onExec(TaskContext ctx) {
    try {
      Process process = db().getActiveProcesses().getProcess(corusPid());
      ActionFactory.newAttemptKillAction(requestor(), db(), process, getRetryCount()).execute(ctx);
    } catch (LogicException e) {
      // no Vm for ID...
      ctx.getTaskOutput().error(e);
      super.abort();
    }
  }  

  /**
   * @see org.sapia.corus.processor.task.ProcessTerminationTask#onKillConfirmed(org.sapia.taskman.TaskContext)
   */
  protected void onKillConfirmed(TaskContext ctx) {
    try {
      Process      process = db().getActiveProcesses().getProcess(corusPid());

      CommandArg nameArg = CommandArgParser.exact(process.getDistributionInfo().getName());
      CommandArg versionArg = CommandArgParser.exact(process.getDistributionInfo().getVersion());      
      Distribution dist = _dists.getDistribution(nameArg, versionArg);
      ProcessConfig conf = dist.getProcess(process.getDistributionInfo()
                                                  .getProcessName());

      synchronized (db()) {
        process.setStatus(Process.RESTARTING);        
        db().getProcessesToRestart().addProcess(process);
        db().getActiveProcesses().removeProcess(process.getProcessID());
      }

      ctx.getTaskOutput().warning("Process '" + process.getProcessID() +
                    "' will be restarted... ");

      synchronized (process) {
        process.releaseLock(this);
        ProcessRestartTask restart = new ProcessRestartTask(dynAddress(), httpPort(), db(),
                                                            process, dist, conf, getPorts());
        process.acquireLock(restart);
        ctx.execAsyncNestedTask("RestartProcessTask", restart);
        
      }
    } catch (LogicException e) {
      ctx.getTaskOutput().error(e);
    } finally {
      super.abort();
    }
  }
  
  /**
   * @see org.sapia.corus.processor.task.ProcessTerminationTask#onMaxRetry(org.sapia.taskman.TaskContext)
   */
  protected boolean onMaxRetry(TaskContext ctx) {
    if (ActionFactory.newForcefulKillAction(dynAddress(), httpPort(), requestor(), db(), corusPid(), -1, getPorts()).execute(ctx)) {
      onKillConfirmed(ctx);

      return true;
    } else {
      return false;
    }
  }

  protected static class ProcessRestartTask implements Task{
    private TCPAddress    _dynSvr;
    private int           _httpPort;
    private ProcessDB     _db;
    private Process       _process;
    private Distribution  _dist;
    private ProcessConfig _conf;
    private PortManager   _ports;

    public ProcessRestartTask(TCPAddress dynServerAddress, int httpPort, ProcessDB db,
                              Process proc, Distribution dist,
                              ProcessConfig conf,
                              PortManager ports) {
      _dynSvr = dynServerAddress;
      _httpPort = httpPort;
      _process = proc;
      _dist    = dist;
      _conf    = conf;
      _db      = db;
      _ports   = ports;
    }
    
    /**
     * @see org.sapia.taskman.Task#exec(org.sapia.taskman.TaskContext)
     */
    public void exec(TaskContext ctx) {
      try {
        if(ActionFactory.newExecProcessAction(_dynSvr, _httpPort, _db, new ProcessInfo(_process, _dist, _conf, true), _ports).execute(ctx)){
          _db.getProcessesToRestart().removeProcess(_process.getProcessID());
          _process.touch();
          _process.setStatus(Process.ACTIVE);        
          _db.getActiveProcesses().addProcess(_process);
        }
      } catch(IOException e) {
        ctx.getTaskOutput().error("Could not restart: " + _conf.getName(), e);
      } finally {
        _process.releaseLock(this);
      }
    }
  }
}
