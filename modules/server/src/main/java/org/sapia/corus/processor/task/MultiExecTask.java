package org.sapia.corus.processor.task;

import java.util.List;

import java.util.Set;

import org.sapia.corus.client.services.configurator.Configurator;
import org.sapia.corus.processor.ProcessRef;
import org.sapia.corus.processor.ProcessRepository;
import org.sapia.corus.taskmanager.core.Task;
import org.sapia.corus.taskmanager.core.TaskExecutionContext;
import org.sapia.corus.taskmanager.core.TaskParams;

public class MultiExecTask extends Task<Void, List<ProcessRef>>{
  
  @Override
  public Void execute(TaskExecutionContext ctx, List<ProcessRef> processRefs) throws Throwable {
    ProcessRepository processes = ctx.getServerContext().getServices().getProcesses();
    Configurator configurator = ctx.getServerContext().getServices().lookup(Configurator.class);

    Set<String> serverTags = configurator.getTags();
    
    for(ProcessRef current : processRefs){
      
      // process is root, starting from 0.
      int startedCount = current.isRoot() ? 0 : processes.getActiveProcessCountFor(current.getCriteria());
      
      // checking for tags
      Set<String> processTags = current.getDist().getTagSet();
      processTags.addAll(current.getProcessConfig().getTagSet());
      ctx.debug("Got server tags: " + serverTags);
      ctx.debug("Got process tags: " + processTags);

      // tags do not match: this process won't be started
      if(processTags.size() > 0 && !serverTags.containsAll(processTags)){
        ctx.warn(
            "Not executing: " + current.getProcessConfig().getName() + 
            " - process tags: " + processTags + 
            " do not match server tags: " + serverTags);
        continue;
      }
      
      for(; startedCount < current.getInstanceCount(); startedCount++){
      
        ctx.info("Preparing execution of process: " + current.toString() + 
            "; started up to now: " + startedCount + "; remaining: " + 
            (current.getInstanceCount() - startedCount));
      
        ctx.info("Executing process instance #" +
              (startedCount+1) + " of " + current.getInstanceCount() + ": " + 
              current.getProcessConfig().getName() + " of distribution: " +
              current.getDist().getName() + ", " + current.getDist().getVersion() + 
              ", " + current.getProfile());
        
        ctx.getTaskManager().execute(
            new ExecTask(), 
            TaskParams.createFor(current.getDist(), current.getProcessConfig(), current.getProfile())
        );
      }
    }

    return null;
  }
  
}
