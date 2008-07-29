package org.sapia.corus.processor.task;

import java.io.IOException;
import java.util.List;

import org.sapia.corus.deployer.config.Distribution;
import org.sapia.corus.deployer.config.ProcessConfig;
import org.sapia.corus.port.PortManager;
import org.sapia.corus.processor.DistributionInfo;
import org.sapia.corus.processor.Process;
import org.sapia.corus.processor.ProcessDB;
import org.sapia.corus.processor.ProcessInfo;
import org.sapia.corus.processor.task.action.ActionFactory;
import org.sapia.taskman.Task;
import org.sapia.taskman.TaskContext;
import org.sapia.ubik.net.TCPAddress;


/**
 * This task initiates the execution of external processes.
 *
 * @author Yanick Duchesne
 * <dl>
 * <dt><b>Copyright:</b><dd>Copyright &#169; 2002-2003 <a href="http://www.sapia-oss.org">Sapia Open Source Software</a>. All Rights Reserved.</dd></dt>
 * <dt><b>License:</b><dd>Read the license.txt file of the jar or visit the
 *        <a href="http://www.sapia-oss.org/license.html">license page</a> at the Sapia OSS web site</dd></dt>
 * </dl>
 */
public class ExecTask implements Task{
  private TCPAddress    _dynSvr;
  private int           _httpPort;
  private Distribution  _dist;
  private ProcessConfig _conf;
  private ProcessDB     _db;
  private PortManager   _ports;
  private String        _profile;

  public ExecTask(TCPAddress dynSvrAddress, int httpPort, ProcessDB db, Distribution dist, ProcessConfig conf,
           String profile, PortManager ports) {
    _dynSvr    = dynSvrAddress;
    _httpPort  = httpPort;
    _db        = db;
    _dist      = dist;
    _conf      = conf;
    _ports     = ports;
    _profile   = profile;
  }

  /**
   * @see org.sapia.taskman.Task#exec(org.sapia.taskman.TaskContext)
   */
  public void exec(TaskContext ctx) {
    Process process = new Process(new DistributionInfo(_dist.getName(),
                                                       _dist.getVersion(),
                                                       _profile,
                                                       _conf.getName()));
    ctx.getTaskOutput().info("Executing process: " + _conf.getName());          
    try{
      if(ActionFactory.newExecProcessAction(_dynSvr, _httpPort, _db, new ProcessInfo(process, _dist, _conf, false), _ports).execute(ctx)){
        _db.getActiveProcesses().addProcess(process);
      }
    }catch(IOException e){
      ctx.getTaskOutput().error("Could not execute: " + _conf.getName(), e);
    }
    ctx.getTaskOutput().info("Execution completed");
  }
}
