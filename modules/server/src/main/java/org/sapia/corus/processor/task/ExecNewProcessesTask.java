package org.sapia.corus.processor.task;

import java.util.List;
import java.util.Set;

import org.sapia.corus.client.common.Arg;
import org.sapia.corus.client.common.StringArg;
import org.sapia.corus.client.exceptions.deployer.DistributionNotFoundException;
import org.sapia.corus.client.services.deployer.Deployer;
import org.sapia.corus.client.services.deployer.dist.Distribution;
import org.sapia.corus.client.services.deployer.dist.ProcessConfig;
import org.sapia.corus.client.services.processor.Process;
import org.sapia.corus.client.services.processor.ProcessDef;
import org.sapia.corus.client.services.processor.Processor;
import org.sapia.corus.processor.ProcessDependencyFilter;
import org.sapia.corus.processor.ProcessRef;
import org.sapia.corus.processor.ProcessRepository;
import org.sapia.corus.processor.StartupLock;
import org.sapia.corus.taskmanager.core.BackgroundTaskConfig;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.log.ProgressQueueTaskLog;

/**
 * Executes the processes corresponding to given {@link ProcessDef}s.
 * 
 * @author yduchesne
 *
 */
public class ExecNewProcessesTask extends Task{
  
  private StartupLock lock;
  private Set<ProcessDef> toStart;
  
  public ExecNewProcessesTask(StartupLock lock, Set<ProcessDef> toStart) {
    this.lock = lock;
    this.toStart = toStart;
  }
  
  @Override
  public Object execute(TaskExecutionContext ctx) throws Throwable {
    
    if(toStart.size() == 0){
      ctx.info("No processes to start, aborting");
      return null;
    }
    
    Deployer deployer = ctx.getServerContext().getServices().getDeployer();
    Processor processor = ctx.getServerContext().getServices().getProcessor();
    ProcessRepository processes = ctx.getServerContext().lookup(ProcessRepository.class);
    ProcessDependencyFilter filter = new ProcessDependencyFilter(new ProgressQueueTaskLog(this, ctx.getLog()));
    
    for(ProcessDef pd:toStart){
      Distribution dist = null;
      Arg distName     = new StringArg(pd.getDist());
      Arg version      = new StringArg(pd.getVersion());
      Arg processName  = new StringArg(pd.getName());
      
      List<Process> activeProcesses = processes.getActiveProcesses().getProcesses(
          distName, 
          version,
          pd.getProfile(),
          processName);
      if(activeProcesses.size() == 0){
        try{
            dist = deployer.getDistribution(distName, version);
        }catch(DistributionNotFoundException e){
          ctx.warn("Could not acquire distribution", e);
          // noop;
        }
        if(dist != null){
          for(ProcessConfig conf: dist.getProcesses(processName)){
            if(conf.containsProfile(pd.getProfile())){
              filter.addRootProcess(dist, conf, pd.getProfile(), 1);
            }
            else{
              ctx.warn("No profile " + pd.getProfile() + " found for " + pd.getProfile());
              ctx.warn("Got profiles " + conf.getProfiles());
            }
          }
        }
        else{
          ctx.warn("No distribution found for " + pd);
        }
      }
      else{
        ctx.warn("Process already started for: " + pd);
      }
    }
    
    filter.filterDependencies(deployer, processor);

    List<ProcessRef> filteredProcesses = filter.getFilteredProcesses();
    if(filteredProcesses.size() > 0){
      
      ctx.info("Dependencies have been resolved; will start the following processes");
      for(ProcessRef ref : filteredProcesses){
        ctx.info(ref.toString());
      }
      
      MultiExecTask exec = new MultiExecTask(lock, filteredProcesses);
      ctx.getTaskManager().executeBackground(
          exec, 
          BackgroundTaskConfig.create()
            .setExecDelay(0)
            .setExecInterval(processor.getConfiguration().getExecIntervalMillis()));
    }
    else{
      ctx.error("No processes found to execute");
    }
    return null;
  }
}
