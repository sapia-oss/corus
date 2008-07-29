package org.sapia.corus.processor.task;

import java.util.List;

import org.sapia.corus.deployer.config.Distribution;
import org.sapia.corus.deployer.config.ProcessConfig;
import org.sapia.corus.port.PortManager;
import org.sapia.corus.processor.ProcessDB;
import org.sapia.corus.processor.StartupLock;
import org.sapia.corus.taskmanager.TaskManager;
import org.sapia.taskman.Abortable;
import org.sapia.taskman.Task;
import org.sapia.taskman.TaskContext;
import org.sapia.taskman.TaskOutput;
import org.sapia.ubik.net.TCPAddress;

public class MultiExecTask implements Task, Abortable{
  
  private TCPAddress    _dynSvr;
  private int           _httpPort;
  private Distribution  _dist;
  private List          _configs;
  private String        _processName, _profile;  
  private ProcessDB     _db;
  private StartupLock   _lock;
  private PortManager   _ports;
  private TaskManager   _taskman;
  private int _instances;
  private int _configCount, _instanceCount;
  
  public MultiExecTask(
      TCPAddress dynSvrAddress, 
      int httpPort, 
      ProcessDB db, 
      StartupLock lock,
      Distribution dist, 
      List configs,
      String processName,
      String profile,
      PortManager ports,
      TaskManager taskman,
      int instances) {
    _dynSvr    = dynSvrAddress;
    _httpPort  = httpPort;
    _db        = db;
    _dist      = dist;
    _configs   = configs;
    _ports     = ports;
    _lock      = lock;
    _taskman   = taskman;
    _processName = processName;
    _profile   = profile;
    _instances = instances;
  }
  
  public void exec(TaskContext ctx) {
    TaskOutput out = ctx.getTaskOutput();
    if(_lock.authorize()){
      if(_configCount < _configs.size()){
        ProcessConfig conf = (ProcessConfig)_configs.get(_configCount);
        if(conf.isInvoke() && _processName == null){
          out.warning("Process for: " + conf.getName() +
          " must be invoked explicitly; not starting");
          _configCount++;
        }
        else if(_instanceCount < _instances){
          out.info("Executing process instance #" + (_instanceCount+1) + " of: " + conf.getName() + 
              " of distribution: " + _dist.getName() + ", " + _dist.getVersion() + ", " + conf.getName());
          ExecTask      exec = new ExecTask(_dynSvr, _httpPort, _db, _dist, conf, _profile, _ports);
          _taskman.execSyncTask("ExecProcessTask", exec);
          _instanceCount++;
          if(_instanceCount >= _instances){
            _configCount++;
            _instanceCount = 0;
          }
        }
      }
    }
    else{
      out.debug("Not executing now; waiting for startup interval exhaustion");
    }
  }
  
  public boolean isAborted() {
    return _configCount >= _configs.size();
  }
  
}
