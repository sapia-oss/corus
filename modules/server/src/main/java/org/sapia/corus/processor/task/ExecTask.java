package org.sapia.corus.processor.task;

import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.DistributionInfo;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.processor.ProcessInfo;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;


/**
 * This task initiates the execution of external processes.
 *
 * @author Yanick Duchesne
 */
public class ExecTask extends Task{
  private Distribution  _dist;
  private ProcessConfig _processConf;
  private String        _profile;

  public ExecTask(Distribution dist, ProcessConfig conf,
           String profile) {
    _dist = dist;
    _processConf = conf;
    _profile = profile;
  }
  
  @Override
  public Object execute(TaskExecutionContext ctx) throws Throwable {
    ProcessorTaskStrategy strategy = ctx.getServerContext().lookup(ProcessorTaskStrategy.class);
    Process process = new Process(new DistributionInfo(_dist.getName(),
                                                       _dist.getVersion(),
                                                       _profile,
                                                       _processConf.getName()));
    ProcessInfo info = new ProcessInfo(process, _dist, _processConf, false);
    ctx.info("Executing process: " + _processConf.getName());          
    
    if(strategy.execProcess(ctx, info, ctx.getServerContext().getProcessProperties())){
      ctx.info(String.format("Added process to active process list: %s", process));          
      ctx.getServerContext().getServices().getProcesses().getActiveProcesses().addProcess(process);
    }
    else{
      ctx.info("Execution completed");
    }
    return null;
  }
}
