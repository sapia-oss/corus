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
  private ProcessRef        _current;
  private int               _startedCount;
  
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
      if(_processRefs.size() > 0 || _current != null){
        if(_current == null){
          _current = _processRefs.remove(0);
          if(_current.isRoot()){
            _startedCount = 0;
          }
          else{
            _startedCount = processes.getProcessCountFor(_current);
          }
        }
        ctx.info("Preparing execution of process: " + _current.toString() + "; started up to now: " + _startedCount + "; remaining: " + 
            (_current.getInstanceCount() - _startedCount));
        Set<String> processTags = _current.getDist().getTagSet();
        processTags.addAll(_current.getProcessConfig().getTagSet());
        ctx.debug("Got server tags: " + serverTags);
        ctx.debug("Got process tags: " + processTags);
        if(processTags.size() > 0 && !serverTags.containsAll(processTags)){
          ctx.warn(
              "Not executing: " + _current.getProcessConfig().getName() + 
              " - process tags: " + processTags + 
              " do not match server tags: " + serverTags);
        }
        else{
          if(_startedCount < _current.getInstanceCount()){
            ctx.info("Executing process instance #" 
                  + (_startedCount+1) + " of " + _current.getInstanceCount() + ": " + _current.getProcessConfig().getName() + " of distribution: " 
                  + _current.getDist().getName() + ", " + _current.getDist().getVersion() + ", " + _current.getProfile());
            ExecTask exec = new ExecTask(_current.getDist(), _current.getProcessConfig(), _current.getProfile());
            ctx.getTaskManager().executeAndWait(exec);
            _startedCount++;
            if(_startedCount >= _current.getInstanceCount()){
              _current = null;
              ctx.info("Process execution completed for: " + _current);

            }
          }
          else{
            _current = null;
            ctx.info("Process execution completed for: " + _current);
          }
        }
      }
    }
    else{
      ctx.debug("Not executing now; waiting for startup interval exhaustion");
    }
    
    if(_processRefs.size() == 0 && _current == null){
      ctx.debug("Completed starting processes");
      abort(ctx);
    }
    else{
      if(_current != null){
        ctx.debug("Pending process instance to start: " + _current);
      }
      if(_processRefs.size() > 0){
        ctx.debug("Still has these processes to start: " + _processRefs);
      }
    }
    return null;
  }
  
}
