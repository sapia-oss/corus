package org.sapia.corus.processor.task;

import java.io.IOException;

import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.ArgFactory;
import org.sapia.corus.client.exceptions.processor.ProcessNotFoundException;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.LockOwner;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.deployer.DistributionDatabase;
import org.sapia.corus.processor.ProcessInfo;
import org.sapia.corus.processor.ProcessRepository;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;


/**
 * This task restarts a given process (it terminates the process and restarts
 * it immediately).
 * 
 * @author Yanick Duchesne
 */
public class RestartTask extends ProcessTerminationTask {
    
  public RestartTask(
      ProcessTerminationRequestor requestor,
      String corusPid, int maxRetry) {
    super(requestor, corusPid, maxRetry);
  }
  
  @Override
  protected void onExec(TaskExecutionContext ctx){
    try {
      ProcessRepository processes = ctx.getServerContext().getServices().getProcesses();
      ProcessorTaskStrategy strategy = ctx.getServerContext().lookup(ProcessorTaskStrategy.class);
      Process process = processes.getActiveProcesses().getProcess(corusPid());
      strategy.attemptKill(ctx, requestor(), process, super.getExecutionCount());
    } catch (ProcessNotFoundException e) {
      ctx.error(e);
      super.abort(ctx);
    }
  }  
  
  @Override
  protected void onKillConfirmed(TaskExecutionContext ctx) {
    try {
      DistributionDatabase dists = ctx.getServerContext().getServices().getDistributions();
      ProcessRepository processes = ctx.getServerContext().getServices().getProcesses();
      Process process = processes.getActiveProcesses().getProcess(corusPid());

      Arg nameArg = ArgFactory.exact(process.getDistributionInfo().getName());
      Arg versionArg = ArgFactory.exact(process.getDistributionInfo().getVersion());      
      Distribution dist = dists.getDistribution(nameArg, versionArg);
      ProcessConfig conf = dist.getProcess(process.getDistributionInfo()
                                                  .getProcessName());

      synchronized (dists) {
        process.setStatus(Process.LifeCycleStatus.RESTARTING);        
        process.clearCommands();
        processes.getProcessesToRestart().addProcess(process);
        processes.getActiveProcesses().removeProcess(process.getProcessID());
      }

      ctx.warn("Process '" + process.getProcessID() +
                    "' will be restarted... ");

      synchronized (process) {
        process.releaseLock(lockOwner());
        LockOwner newLockOwner = new LockOwner();
        ProcessRestartTask restart = new ProcessRestartTask(newLockOwner,
                                                            process, 
                                                            dist, 
                                                            conf);
        process.acquireLock(newLockOwner);
        process.save();
        ctx.getTaskManager().execute(restart);
      }
    } catch (Exception e) {
      ctx.error(e);
    } finally {
      super.abort(ctx);
    }
  }
  
  
  @Override
  protected void onMaxExecutionReached(TaskExecutionContext ctx)
      throws Throwable {
    ProcessorTaskStrategy strategy = ctx.getServerContext().lookup(ProcessorTaskStrategy.class);
    if(strategy.forcefulKill(ctx, requestor(), corusPid())){
      onKillConfirmed(ctx);
    }
  }

  protected static class ProcessRestartTask extends Task{
    private Process       _process;
    private Distribution  _dist;
    private ProcessConfig _conf;
    private LockOwner     _lockOwner;

    public ProcessRestartTask(LockOwner lockOwner,
                              Process proc, 
                              Distribution dist,
                              ProcessConfig conf) {
      _lockOwner = lockOwner;
      _process   = proc;
      _dist      = dist;
      _conf      = conf;
    }
    
    @Override
    public Object execute(TaskExecutionContext ctx) throws Throwable {
      try {
        ProcessRepository repository = ctx.getServerContext().getServices().getProcesses();
        ProcessorTaskStrategy strategy = ctx.getServerContext().lookup(ProcessorTaskStrategy.class);
        if(strategy.execProcess(ctx, new ProcessInfo(_process, _dist, _conf, true), ctx.getServerContext().getProcessProperties())){
          repository.getProcessesToRestart().removeProcess(_process.getProcessID());
          _process.touch();
          _process.clearCommands();
          _process.setStatus(Process.LifeCycleStatus.ACTIVE);        
          repository.getActiveProcesses().addProcess(_process);
        }
      } catch(IOException e) {
        ctx.error("Could not restart: " + _conf.getName(), e);
      } finally {
        _process.releaseLock(_lockOwner);
        _process.save();
      }
      return null;
    }
  }
}
