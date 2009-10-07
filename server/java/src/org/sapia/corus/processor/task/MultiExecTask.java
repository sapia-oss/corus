package org.sapia.corus.processor.task;

import java.util.List;
import java.util.Set;

import org.sapia.corus.admin.services.configurator.Configurator;
import org.sapia.corus.processor.ProcessRef;
import org.sapia.corus.processor.ProcessRepository;
import org.sapia.corus.processor.StartupLock;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;

public class MultiExecTask extends Task{
  
  private List<ProcessRef>  _processRefs;
  private StartupLock       _lock;
  
  public MultiExecTask(
      StartupLock lock,
      List<ProcessRef> processRefs) {
    _processRefs = processRefs;
    _lock        = lock;
  }
  
  @Override
  public Object execute(TaskExecutionContext ctx) throws Throwable {
    ProcessRepository processes = ctx.getServerContext().getServices().getProcesses();
    Configurator configurator = ctx.getServerContext().getServices().lookup(Configurator.class);
    if(_lock.authorize()){
      Set<String> serverTags = configurator.getTags();
      if(_processRefs.size() > 0){
        ProcessRef processRef = _processRefs.remove(0);
        ctx.info("Starting execution of process: " + processRef.toString());
        Set<String> processTags = processRef.getDist().getTagSet();
        processTags.addAll(processRef.getProcessConfig().getTagSet());
        ctx.debug("Got server tags: " + serverTags);
        ctx.debug("Got process tags: " + processTags);
        if(processTags.size() > 0 && !serverTags.containsAll(processTags)){
          ctx.warn(
              "Not executing: " + processRef.getProcessConfig().getName() + 
              " - process tags: " + processTags + 
              " do not match server tags: " + serverTags);
        }
        else{
          int instanceCount = processes.getProcessCountFor(processRef);
          if(instanceCount < processRef.getInstanceCount()){
            ctx.info("Executing process instance #" 
                  + (instanceCount+1) + " of: " + processRef.getProcessConfig().getName() + " of distribution: " 
                  + processRef.getDist().getName() + ", " + processRef.getDist().getVersion() + ", " + processRef.getProfile());
            ExecTask exec = new ExecTask(processRef.getDist(), processRef.getProcessConfig(), processRef.getProfile());
            ctx.getTaskManager().executeAndWait(exec);
          }
        }
      }
    }
    else{
      ctx.debug("Not executing now; waiting for startup interval exhaustion");
    }
    if(_processRefs.size() <= 0){
      ctx.debug("Completed starting processes");
      abort(ctx);
    }
    else{
      ctx.debug("Still has these processes to start: " + _processRefs);
    }
    return null;
  }
  
}
