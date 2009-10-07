package org.sapia.corus.processor;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sapia.corus.ModuleHelper;
import org.sapia.corus.admin.Arg;
import org.sapia.corus.admin.ArgFactory;
import org.sapia.corus.admin.services.deployer.Deployer;
import org.sapia.corus.admin.services.deployer.dist.Distribution;
import org.sapia.corus.admin.services.deployer.dist.ProcessConfig;
import org.sapia.corus.admin.services.processor.ExecConfig;
import org.sapia.corus.admin.services.processor.ProcStatus;
import org.sapia.corus.admin.services.processor.Process;
import org.sapia.corus.admin.services.processor.Processor;
import org.sapia.corus.admin.services.processor.ProcessorConfiguration;
import org.sapia.corus.admin.services.processor.ProcessorConfigurationImpl;
import org.sapia.corus.admin.services.processor.Process.ProcessTerminationRequestor;
import org.sapia.corus.db.DbModule;
import org.sapia.corus.deployer.DistributionDatabase;
import org.sapia.corus.deployer.event.UndeploymentEvent;
import org.sapia.corus.event.EventDispatcher;
import org.sapia.corus.exceptions.CorusException;
import org.sapia.corus.exceptions.LockException;
import org.sapia.corus.exceptions.LogicException;
import org.sapia.corus.http.HttpModule;
import org.sapia.corus.interop.Status;
import org.sapia.corus.processor.task.BootstrapExecConfigStartTask;
import org.sapia.corus.processor.task.EndUserExecConfigStartTask;
import org.sapia.corus.processor.task.KillTask;
import org.sapia.corus.processor.task.MultiExecTask;
import org.sapia.corus.processor.task.ProcessCheckTask;
import org.sapia.corus.processor.task.ProcessorTaskStrategy;
import org.sapia.corus.processor.task.ProcessorTaskStrategyImpl;
import org.sapia.corus.processor.task.RestartTask;
import org.sapia.corus.processor.task.ResumeTask;
import org.sapia.corus.processor.task.SuspendTask;
import org.sapia.corus.taskmanager.core.BackgroundTaskConfig;
import org.sapia.corus.taskmanager.core.TaskConfig;
import org.sapia.corus.taskmanager.core.TaskLogProgressQueue;
import org.sapia.corus.taskmanager.core.TaskManager;
import org.sapia.corus.util.ProgressQueue;
import org.sapia.corus.util.ProgressQueueImpl;
import org.sapia.ubik.rmi.interceptor.Interceptor;

/**
 * Implements the <code>Processor</code> interface.
 *
 * @author Yanick Duchesne
 */
public class ProcessorImpl extends ModuleHelper implements Processor {
    
  private ProcessorConfigurationImpl _configuration = new ProcessorConfigurationImpl();
  
  private ProcessRepository      _processes;
  private ExecConfigDatabaseImpl _execConfigs;
  private StartupLock            _startLock;  
  
  
  public ProcessorConfiguration getConfiguration() {
    return _configuration;
  }
  
  public ProcessorConfiguration createConfiguration(){
    return _configuration;
  }

  @SuppressWarnings(value={"unchecked"})
  public void init() throws Exception {

    _startLock = new StartupLock(_configuration.getStartIntervalMillis());
    
    DbModule     db    = services().lookup(DbModule.class);
    services().bind(ProcessorTaskStrategy.class, new  ProcessorTaskStrategyImpl());
    
    //// intializing process repository
    _execConfigs = new ExecConfigDatabaseImpl(db.getDbMap("processor.execConfigs"));
    services().bind(ExecConfigDatabase.class, _execConfigs);
    
    
    ProcessDatabase suspended = new ProcessDatabaseImpl(db.getDbMap("processor.suspended"));
    ProcessDatabase active = new ProcessDatabaseImpl(db.getDbMap("processor.active"));
    ProcessDatabase toRestart = new ProcessDatabaseImpl(db.getDbMap("processor.toRestart"));

    _processes = new ProcessRepositoryImpl(suspended, active, toRestart);
    services().bind(ProcessRepository.class, _processes);

    // here we "touch" the process objects to prevent their automatic 
    // termination (the Corus server might have been down for a period
    // of time that is longer then some process' tolerated idle delay).                                                                    
    List<Process>    processes = (List<Process>) active.getProcesses();
    Process proc;

    for (int i = 0; i < processes.size(); i++) {
      proc = (Process) processes.get(i);
      proc.touch();
    }
  }
  
  public void start() throws Exception {
    TaskManager  tm     = lookup(TaskManager.class);
    HttpModule   module = lookup(HttpModule.class);
    ProcessorExtension ext = new ProcessorExtension(this);
    module.addHttpExtension(ext);
    
    BootstrapExecConfigStartTask boot = new BootstrapExecConfigStartTask(_startLock);
    
    tm.executeBackground(
        boot,
        BackgroundTaskConfig.create()
          .setExecDelay(_configuration.getBootExecDelayMillis())
          .setExecInterval(_configuration.getExecIntervalMillis()));
    
    ProcessCheckTask check = new ProcessCheckTask();
    tm.executeBackground(
        check, 
        BackgroundTaskConfig.create()
          .setExecDelay(0)
          .setExecInterval(_configuration.getProcessCheckIntervalMillis()));

    lookup(EventDispatcher.class).addInterceptor(UndeploymentEvent.class, new ProcessorInterceptor());
  }

  /**
   * @see org.sapia.soto.Service#dispose()
   */
  public void dispose() {
  }

  /*////////////////////////////////////////////////////////////////////
                         Module INTERFACE METHODS
  ////////////////////////////////////////////////////////////////////*/

  /**
   * @see org.sapia.corus.admin.Module#getRoleName()
   */
  public String getRoleName() {
    return Processor.ROLE;
  }

  /*////////////////////////////////////////////////////////////////////
                       Processor INTERFACE METHODS
  ////////////////////////////////////////////////////////////////////*/
  
  public ProgressQueue exec(String execConfigName) {
    ProgressQueue progress = new ProgressQueueImpl();
    TaskManager  taskman = lookup(TaskManager.class);
    EndUserExecConfigStartTask start = new EndUserExecConfigStartTask(execConfigName, _startLock);
    try{
      taskman.executeAndWait(
          start, TaskConfig.create(new TaskLogProgressQueue(progress))).get();
    }catch(InvocationTargetException e){
      // noop
    }catch(InterruptedException e){
      progress.error(e);
      progress.close();
    }

    return progress;
    
  }

  public ProgressQueue exec(Arg distName, Arg version, String profile,
    Arg processName, int instances) {
    try {
      Deployer     deployer = lookup(Deployer.class);
      List<Distribution> dists = deployer.getDistributions(distName, version);
      if (dists.size() == 0) {
        throw new LogicException("No distribution for " + distName + ", " +
          version);
      }
      ProgressQueueImpl q = new ProgressQueueImpl();      
      ProcessDependencyFilter filter = new ProcessDependencyFilter(q);
      for(int i = 0; i < dists.size(); i++){
        Distribution dist = dists.get(i);
        List<ProcessConfig> configs;
        if(processName == null){
          configs = new ArrayList<ProcessConfig>();
          List<ProcessConfig> temp = dist.getProcesses();
          for(ProcessConfig pc: temp){
            if(!pc.isInvoke()){
              configs.add(pc);
            }
          }
        }
        else{
          configs = dist.getProcesses(processName);
        }
        if(configs.size() == 0){
          q.warning("Could not find any process to start for: " + distName + ", " + version
              + ", " + processName);
        }
        else{
          for(ProcessConfig config:configs){
            filter.addRootProcess(dist, config, profile);
          }
        }
      }
      List<ProcessRef> toStart = new ArrayList<ProcessRef>();
      filter.filterDependencies(deployer, this);
      for(ProcessRef fp:filter.getFilteredProcesses()){
        q.info("Scheduling execution of process: " + fp);
        ProcessRef copy = new ProcessRef(fp.getDist(), fp.getProcessConfig(), fp.getProfile(), 0);
        copy.setInstanceCount(instances);
        toStart.add(copy);
      }
      
      TaskManager taskman = lookup(TaskManager.class); 
      MultiExecTask  exec = new MultiExecTask(_startLock, toStart);
      taskman.executeBackground(
          exec,
          BackgroundTaskConfig.create()
            .setExecDelay(0)
            .setExecInterval(_configuration.getExecIntervalMillis()));
      
      q.close();
      return q;
    } catch (Exception e) {
      ProgressQueue q = new ProgressQueueImpl();
      q.error(e);
      q.close();

      return q;
    }
  }

  public ProgressQueue exec(Arg distName, Arg version, String profile,
    int instances) {
    return exec(distName, version, profile, null, instances);
  }
  
  public void addExecConfig(ExecConfig conf) throws LogicException{
    if(conf.getName() == null) {
      throw new LogicException("Execution configuration must have a name");
    }
    this._execConfigs.addConfig(conf);
  }
  
  public List<ExecConfig> getExecConfigs() {
    return _execConfigs.getConfigs();
  }
  
  public void removeExecConfig(Arg name) {
    _execConfigs.removeConfigsFor(name);
  }

  public void kill(Arg distName, Arg version, String profile,
      Arg processName, boolean suspend) throws CorusException {
    List<Process> procs = _processes.getActiveProcesses().getProcesses(distName, version,
        profile, processName);
    TaskManager tm;
    KillTask kill;
    Process proc;

    try {
      tm = lookup(TaskManager.class);
    } catch (Exception e) {
      throw new CorusException(e);
    }

    for (int i = 0; i < procs.size(); i++) {
      proc = (Process) procs.get(i);

      if (suspend) {
        SuspendTask susp = new SuspendTask(
            ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN,
            proc.getProcessID(), 
            proc.getMaxKillRetry());
        
        tm.executeBackground(
            susp,
            BackgroundTaskConfig.create()
              .setExecDelay(0)
              .setExecInterval(_configuration.getKillIntervalMillis()));
      } else {
        kill   = new KillTask(
            ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN,
            proc.getProcessID(), proc.getMaxKillRetry());
        tm.executeBackground(kill,
            BackgroundTaskConfig.create()
              .setExecDelay(0)
              .setExecInterval(_configuration.getKillIntervalMillis()));
      }
    }
  }

  public void kill(Arg distName, Arg version, 
      boolean suspend) throws CorusException {
    kill(distName, version, null, suspend);
  }
  public void kill(Arg distName, Arg version, String profile,
    boolean suspend) throws CorusException {
    List<Process>          procs = _processes.getActiveProcesses().getProcesses(distName,
        version, profile);
    TaskManager          tm;
    KillTask               kill;
    Process                proc;
    try {
      tm = lookup(TaskManager.class);
    } catch (Exception e) {
      throw new CorusException(e);
    }

    for (int i = 0; i < procs.size(); i++) {
      proc = (Process) procs.get(i);

      if (suspend) {
        SuspendTask susp = new SuspendTask(
            ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN, 
            proc.getProcessID(), 
            proc.getMaxKillRetry());
        tm.executeBackground(
            susp,
            BackgroundTaskConfig.create()
              .setExecDelay(0)
              .setExecInterval(_configuration.getKillIntervalMillis()));
      } else {
        kill   = new KillTask(
            ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN,
            proc.getProcessID(), proc.getMaxKillRetry());
        tm.executeBackground(
            kill,
            BackgroundTaskConfig.create()
              .setExecDelay(0)
              .setExecInterval(_configuration.getKillIntervalMillis()));
      }
    }
  }

  public void kill(String corusPid, boolean suspend) throws CorusException {
    try {

      KillTask               kill;
      Process                proc = _processes.getActiveProcesses().getProcess(corusPid);

      if (suspend) {
        SuspendTask susp = new SuspendTask(
            ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN, 
            corusPid, proc.getMaxKillRetry());
        lookup(TaskManager.class).executeBackground(
            susp,
            BackgroundTaskConfig.create()
              .setExecDelay(0)
              .setExecInterval(_configuration.getKillIntervalMillis()));
      } else {
        kill   = new KillTask(
            ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN,
            corusPid, proc.getMaxKillRetry());
        lookup(TaskManager.class).executeBackground(
            kill,
            BackgroundTaskConfig.create()
              .setExecDelay(0)
              .setExecInterval(_configuration.getKillIntervalMillis()));
      }
    } catch (Exception e) {
      throw new CorusException(e);
    }
  }

  public void restartByAdmin(String pid) throws CorusException {
    doRestart(pid, ProcessTerminationRequestor.KILL_REQUESTOR_ADMIN);
  }

  public void restart(String pid) throws CorusException {
    doRestart(pid, ProcessTerminationRequestor.KILL_REQUESTOR_PROCESS);
  }
  
  private void doRestart(String dynId, ProcessTerminationRequestor origin) throws CorusException {
    try {
      Process     proc    = _processes.getActiveProcesses().getProcess(dynId);
      RestartTask restart = new RestartTask(
          origin, 
          dynId, proc.getMaxKillRetry());

      lookup(TaskManager.class).executeBackground(
          restart,
          BackgroundTaskConfig.create()
            .setExecDelay(0)
            .setExecInterval(_configuration.getKillIntervalMillis()));
      
    } catch (CorusException e) {
      throw e;
    } catch (Exception e) {
      throw new CorusException(e);
    }
  }

  public ProgressQueue resume() {
    Iterator<Process>    procs  = _processes.getSuspendedProcesses().getProcesses().iterator();
    TaskManager tm;
    ResumeTask  resume;
    Process     proc;

    try {
      tm = lookup(TaskManager.class);
    } catch (Exception e) {
      ProgressQueue q = new ProgressQueueImpl();
      q.error(e);
      q.close();

      return q;
    }

    DistributionDatabase store = lookup(DistributionDatabase.class);

    Distribution  dist;
    ProcessConfig conf;
    int           restartCount = 0;

    while (procs.hasNext()) {
      proc = (Process) procs.next();
      Arg nameArg = ArgFactory.exact(proc.getDistributionInfo().getName());
      Arg versionArg = ArgFactory.exact(proc.getDistributionInfo().getVersion());      
      try {
 
        dist = store.getDistribution(nameArg, versionArg);
      } catch (LogicException e) {
        store.removeDistribution(nameArg, versionArg);
        continue;
      }

      conf = dist.getProcess(proc.getDistributionInfo().getProcessName());

      if (conf == null) {
        ProgressQueue q = new ProgressQueueImpl();
        q.error("No process named '" +
          proc.getDistributionInfo().getProcessName() + "'");
        q.close();

        return q;
      }

      proc.touch();

      try {
        resume = new ResumeTask(proc, dist, conf);
      } catch (LockException e) {
        ProgressQueue q = new ProgressQueueImpl();
        q.error(e);
        q.close();

        return q;
      }

      tm.execute(resume);
      restartCount++;
    }

    ProgressQueue q = new ProgressQueueImpl();

    if (restartCount == 0) {
      q.warning("No suspended VMs to restart.");
      q.close();
    } else {
      q.warning("Restarted " + restartCount + " suspended VMs.");
      q.close();
    }

    return q;
  }

  public Process getProcess(String corusPid) throws LogicException {
    return _processes.getActiveProcesses().getProcess(corusPid);
  }

  public List<Process> getProcesses() {
    return _processes.getProcesses();
  }

  public List<Process> getProcesses(Arg distName, Arg version, String profile,
    Arg processName) {
    return _processes.getProcesses(distName, version, profile, processName);
  }

  public List<Process> getProcesses(Arg distName, Arg version, String profile) {
    return _processes.getProcesses(distName, version, profile);
  }
  
  public List<Process> getProcesses(Arg distName, Arg version) {
    return _processes.getProcesses(distName, version);
  }

  public List<Process> getProcesses(Arg distName) {
    return _processes.getProcesses(distName);
  }
  
  public List<Process> getProcessesWithPorts() {
    List<Process> toReturn = new ArrayList<Process>();
    List<Process> processes = _processes.getProcesses();
    for(int i = 0; i < processes.size(); i++){
      Process p = processes.get(i);
      if(p.getActivePorts().size() > 0){
        toReturn.add(p);
      }
    }
    return toReturn;
  }  

  public List<Status> getStatus() {
    return copyStatus(getProcesses());
  }

  public List<Status> getStatus(Arg distName, Arg version, String profile,
    Arg processName) {
    return copyStatus(getProcesses(distName, version, profile, processName));
  }
  
  public List<Status> getStatus(Arg distName, Arg version, String profile) {
    return copyStatus(getProcesses(distName, version, profile));
  }

  public List<Status> getStatus(Arg distName, Arg version) {
    return copyStatus(getProcesses(distName, version));
  }

  public List<Status> getStatus(Arg distName) {
    return copyStatus(getProcesses(distName));
  }

  public ProcStatus getStatusFor(String corusPid) throws LogicException {
    Process proc = getProcess(corusPid);

    return copyStatus(proc);
  }

  private List<Status> copyStatus(List<Process> processes) {
    List<Status>   stat   = new ArrayList<Status>(processes.size());
    for (Process p:processes) {
      stat.add(copyStatus(p));
    }

    return stat;
  }
  
  private ProcStatus copyStatus(Process p) {
    return new ProcStatus(p);
  }
  
  public class ProcessorInterceptor implements Interceptor{
   
    public void onUndeploymentEvent(UndeploymentEvent evt){
      _execConfigs.removeProcessesForDistribution(evt.getDistribution());
    }
  }
}
