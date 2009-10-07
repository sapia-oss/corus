package org.sapia.corus.processor.task;

import java.io.IOException;

import org.sapia.corus.CorusRuntime;
import org.sapia.corus.admin.services.deployer.dist.Distribution;
import org.sapia.corus.admin.services.deployer.dist.ProcessConfig;
import org.sapia.corus.admin.services.processor.Process;
import org.sapia.corus.exceptions.LockException;
import org.sapia.corus.processor.ProcessInfo;
import org.sapia.corus.processor.ProcessRepository;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;


/**
 * This task resumes a suspended process.
 * 
 * @author Yanick Duchesne
 */
public class ResumeTask extends Task{
  private Process       _process;
  private Distribution  _dist;
  private ProcessConfig _conf;

  public ResumeTask(Process proc,
                    Distribution dist, 
                    ProcessConfig conf)
             throws LockException {
    _process = proc;
    _dist    = dist;
    _conf    = conf;
    proc.acquireLock(this);
  }

  @Override
  public Object execute(TaskExecutionContext ctx) throws Throwable {
    _process.touch();
    try{
      ProcessRepository processes = ctx.getServerContext().getServices().getProcesses();
      ProcessorTaskStrategy strategy = ctx.getServerContext().lookup(ProcessorTaskStrategy.class);
      if(strategy.execProcess(ctx, new ProcessInfo(_process, _dist, _conf, true), CorusRuntime.getProcessProperties())){
        processes.getSuspendedProcesses().removeProcess(_process.getProcessID());
        _process.setStatus(Process.LifeCycleStatus.ACTIVE);
        processes.getActiveProcesses().addProcess(_process);
      }
      else{
        ctx.error("Process: " + _process.getProcessID() + " will not be resumed");
      }
    }catch(IOException e){
      ctx.error("Error restarting process: " + _process.getProcessID(), e);
    }
    return null;
  }
}
