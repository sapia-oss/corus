package org.sapia.corus.processor.task;

import java.util.List;
import java.util.Set;

import org.sapia.corus.admin.Arg;
import org.sapia.corus.admin.StringArg;
import org.sapia.corus.admin.services.deployer.Deployer;
import org.sapia.corus.admin.services.deployer.dist.Distribution;
import org.sapia.corus.admin.services.deployer.dist.ProcessConfig;
import org.sapia.corus.admin.services.processor.Process;
import org.sapia.corus.admin.services.processor.ProcessDef;
import org.sapia.corus.admin.services.processor.Processor;
import org.sapia.corus.exceptions.LogicException;
import org.sapia.corus.processor.ProcessDependencyFilter;
import org.sapia.corus.processor.ProcessRef;
import org.sapia.corus.processor.ProcessRepository;
import org.sapia.corus.processor.StartupLock;
import org.sapia.corus.taskmanager.core.BackgroundTaskConfig;
import org.sapia.corus.taskmanager.core.ProgressQueueTaskLog;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;

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
    
    Deployer deployer = ctx.getServerContext().lookup(Deployer.class);
    Processor processor = ctx.getServerContext().lookup(Processor.class);
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
        }catch(LogicException e){
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
      ctx.getLog().error(this, "No processes found to execute");
    }
    return null;
  }
}
