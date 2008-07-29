package org.sapia.corus.processor.task.action;

import org.sapia.corus.CorusException;
import org.sapia.corus.CorusRuntime;
import org.sapia.corus.LogicException;
import org.sapia.corus.admin.CommandArg;
import org.sapia.corus.admin.CommandArgParser;
import org.sapia.corus.deployer.Deployer;
import org.sapia.corus.deployer.config.Distribution;
import org.sapia.corus.port.PortManager;
import org.sapia.corus.processor.Process;
import org.sapia.corus.processor.ProcessDB;
import org.sapia.corus.processor.task.ExecTask;
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
public class RestartVmAction implements Action{
  
  private TCPAddress  _addr;
  private int         _httpPort;
  private ProcessDB   _db;
  private Process     _proc;
  private PortManager _ports;
  
  public RestartVmAction(TCPAddress addr, int httpPort, ProcessDB db, Process proc, PortManager ports){
    _addr = addr;
    _httpPort = httpPort;
    _db = db;
    _proc = proc;
    _ports = ports;
  }
  
  /**
   * @see org.sapia.corus.taskmanager.Action#execute(org.sapia.taskman.TaskContext)
   */
  public boolean execute(TaskContext ctx){
    Distribution dist; 
    ctx.getTaskOutput().debug("Executing process");    
    try{
      Deployer deployer = (Deployer)CorusRuntime.getCorus().lookup(Deployer.ROLE);
      
      CommandArg nameArg = CommandArgParser.exact(_proc.getDistributionInfo().getName());
      CommandArg versionArg = CommandArgParser.exact(_proc.getDistributionInfo().getVersion());      
      
      dist = deployer.getDistribution(nameArg, versionArg);
    }catch(LogicException e){
      e.printStackTrace();
      ctx.getTaskOutput().error("Could not find corresponding distribution; process " + _proc.getProcessID() + " will not be restarted", e);
      return false;
    }catch(CorusException e){
      e.printStackTrace();
      ctx.getTaskOutput().error("Could not look up Deployer module; process " + _proc.getProcessID() + " will not be restarted", e);
      return false;
    }    
    
    ExecTask exec = new ExecTask(_addr, 
                                 _httpPort,
                                 _db,
                                 dist,
                                 dist.getProcess(_proc.getDistributionInfo().getProcessName()),
                                 _proc.getDistributionInfo().getProfile(), _ports);
    exec.exec(ctx);
    //ctx.execSyncNestedTask("ExecTask", exec);
    return true;
  }

}
